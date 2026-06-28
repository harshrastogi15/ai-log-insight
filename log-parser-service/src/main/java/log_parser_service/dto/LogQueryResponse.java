package log_parser_service.dto;

import log_parser_service.model.StructuredLog;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LogQueryResponse {

    private String question;
    private String answer;
    private int logsAnalyzed;
    private List<StructuredLog> retrievedLogs;
}