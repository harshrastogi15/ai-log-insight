package log_parser_service.dto;

import lombok.Data;

@Data
public class LogQueryRequest {

    private String question;

    private String level;

    private String service;
}