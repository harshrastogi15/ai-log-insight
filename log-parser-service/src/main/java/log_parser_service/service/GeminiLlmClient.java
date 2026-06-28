package log_parser_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiLlmClient implements LlmClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    @Override
    public String answer(String question, String logContext) {
        String prompt = String.format("""
                You are an expert log analyst.
                Answer the question based ONLY on the log entries below.
                Be concise, mention specific services and error messages.
                If you can identify a root cause, highlight it.
                
                Question: %s
                
                Log entries:
                %s
                """, question, logContext);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            Map response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List candidates = (List) response.get("candidates");
            Map content    = (Map) ((Map) candidates.get(0)).get("content");
            List parts     = (List) content.get("parts");
            return (String) ((Map) parts.get(0)).get("text");

        } catch (Exception e) {
            if (e.getMessage().contains("429")) {
                log.warn("Gemini rate limited — returning fallback");
                return "Rate limited. Log context:\n" + logContext;
            }
            throw e;
        }
    }
}