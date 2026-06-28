package log_parser_service.service;

import log_parser_service.model.StructuredLog;
import log_parser_service.repository.StructuredLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaLogConsumer {

    private final LogParserService parserService;
    private final StructuredLogRepository repository;
    private final EmbeddingService embeddingService;

    @KafkaListener(topics = "raw-logs-topic", groupId = "log-parser-group")
    public void consume(String rawLog) {
        log.info("Received: {}", rawLog);

        try {
            StructuredLog structuredLog = parserService.parse(rawLog);

            StructuredLog saved = repository.save(structuredLog);

            float[] embedding = embeddingService.embed(structuredLog.getMessage());
            String vectorLiteral = embeddingService.toVectorLiteral(embedding);

            repository.updateEmbedding(saved.getId(), vectorLiteral);

            log.info("Saved id={} level={} service={} with embedding",
                    saved.getId(), saved.getLevel(), saved.getService());

        } catch (Exception e) {
            log.error("Failed to process log: {}", e.getMessage());
        }
    }
}