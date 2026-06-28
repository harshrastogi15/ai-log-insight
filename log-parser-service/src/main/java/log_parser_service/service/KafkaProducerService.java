package log_parser_service.service;

import log_parser_service.dto.RawLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendLog(RawLogRequest request) {

        kafkaTemplate.send("raw-logs-topic", request.getRawMessage());
    }
}