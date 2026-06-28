package log_parser_service.controller;

import log_parser_service.dto.RawLogRequest;
import log_parser_service.model.StructuredLog;
import log_parser_service.repository.StructuredLogRepository;
import log_parser_service.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import log_parser_service.dto.LogQueryRequest;
import log_parser_service.dto.LogQueryResponse;
import log_parser_service.service.LogQueryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final KafkaProducerService producerService;
    private final StructuredLogRepository repository;
    private final LogQueryService queryService;

    @PostMapping
    public ResponseEntity<Map<String, String>> ingestLog(@RequestBody RawLogRequest request) {
        if (request.getRawMessage() == null || request.getRawMessage().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "rawMessage must not be blank"));
        }
        producerService.sendLog(request);
        return ResponseEntity.accepted()
                .body(Map.of("status", "accepted"));
    }

    @GetMapping
    public ResponseEntity<List<StructuredLog>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service
    ) {
        List<StructuredLog> logs;

        if (level != null && service != null) {
            logs = repository.findByLevelAndService(level.toUpperCase(), service);
        } else if (level != null) {
            logs = repository.findByLevel(level.toUpperCase());
        } else if (service != null) {
            logs = repository.findByService(service);
        } else {
            logs = repository.findTop100ByOrderByCreatedAtDesc();
        }

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Long> byLevel = Map.of(
                "ERROR", repository.countByLevel("ERROR"),
                "WARN",  repository.countByLevel("WARN"),
                "INFO",  repository.countByLevel("INFO"),
                "DEBUG", repository.countByLevel("DEBUG"),
                "FATAL", repository.countByLevel("FATAL")
        );
        return ResponseEntity.ok(Map.of(
                "total", repository.count(),
                "byLevel", byLevel
        ));
    }

    @PostMapping("/query")
    public ResponseEntity<LogQueryResponse> queryLogs(
            @RequestBody LogQueryRequest request) {

        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        LogQueryResponse response = queryService.query(request);
        return ResponseEntity.ok(response);
    }


}