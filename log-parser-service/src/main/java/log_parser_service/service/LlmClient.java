package log_parser_service.service;

import java.util.List;


public interface LlmClient {

    /**
     * @param question   the user's natural language question
     * @param logContext the retrieved log entries as formatted string
     * @return AI-generated answer
     */
    String answer(String question, String logContext);
}