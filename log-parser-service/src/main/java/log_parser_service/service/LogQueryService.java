package log_parser_service.service;

import log_parser_service.dto.LogQueryRequest;
import log_parser_service.dto.LogQueryResponse;
import log_parser_service.model.StructuredLog;
import log_parser_service.repository.StructuredLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final EmbeddingClient embeddingClient;
    private final LlmClient llmClient;
    private final StructuredLogRepository repository;

    @Value("${log.query.top-k:15}")
    private int topK;

    public LogQueryResponse query(LogQueryRequest request) {

        // embed question
        float[] vector = embeddingClient.embed(request.getQuestion());
        String vectorLiteral = embeddingClient.toVectorLiteral(vector);

        // hybrid search
        String level   = blankToNull(request.getLevel());
        String service = blankToNull(request.getService());

        List<StructuredLog> hits = repository.findSimilarLogsFiltered(
                vectorLiteral, level, service, topK
        );

        if (hits.isEmpty()) {
            return LogQueryResponse.builder()
                    .question(request.getQuestion())
                    .answer("No logs found matching your query.")
                    .logsAnalyzed(0)
                    .retrievedLogs(List.of())
                    .build();
        }

        // build context
        String logContext = hits.stream()
                .map(l -> String.format("[%s] %s (%s): %s",
                        l.getTimestamp(), l.getLevel(),
                        l.getService(), l.getMessage()))
                .collect(Collectors.joining("\n"));

        // get AI answer — doesn't know or care which LLM
        String answer = llmClient.answer(request.getQuestion(), logContext);

        return LogQueryResponse.builder()
                .question(request.getQuestion())
                .answer(answer)
                .logsAnalyzed(hits.size())
                .retrievedLogs(hits)
                .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}