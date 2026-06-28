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
public class EmbeddingService implements EmbeddingClient{

    @Value("${embedding.api-url}")
    private String apiUrl;

    private final RestClient restClient;

    public EmbeddingService() {
        this.restClient = RestClient.create();
    }

    public float[] embed(String text) {
        log.debug("Calling embedding service for: {}", text);

        Map response = restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .body(Map.class);

        List<Double> vector = (List<Double>) response.get("embedding");

        float[] result = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.get(i).floatValue();
        }

        log.info("Embedding generated — {} dimensions", result.length);
        return result;
    }

    public String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}