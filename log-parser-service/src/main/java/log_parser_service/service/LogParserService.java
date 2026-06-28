package log_parser_service.service;

import log_parser_service.model.StructuredLog;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogParserService {

    // Matches: 2024-03-15 09:12:03 ERROR auth-service Login failed...
    // Also matches: 2024-03-15 09:12:03 ERROR [auth-service] Login failed...
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}(?:\\s\\d{2}:\\d{2}:\\d{2})?)\\s+" +  // date + optional time
                    "(INFO|ERROR|WARN|DEBUG|FATAL|TRACE)\\s+" +                   // level
                    "\\[?([\\w\\-./]+)]?\\s+" +                                   // service
                    "(.+)$",                                                      // message
            Pattern.CASE_INSENSITIVE
    );

    public StructuredLog parse(String rawLog) {
        if (rawLog == null || rawLog.isBlank()) {
            throw new IllegalArgumentException("Raw log must not be blank");
        }

        Matcher matcher = LOG_PATTERN.matcher(rawLog.trim());

        if (!matcher.find()) {
            // Instead of throwing — store it as UNKNOWN so bad logs don't crash Kafka
            return StructuredLog.builder()
                    .timestamp("unknown")
                    .level("UNKNOWN")
                    .service("unknown")
                    .message(rawLog.trim())
                    .rawLog(rawLog)
                    .build();
        }

        return StructuredLog.builder()
                .timestamp(matcher.group(1))
                .level(matcher.group(2).toUpperCase())
                .service(matcher.group(3))
                .message(matcher.group(4).trim())
                .rawLog(rawLog)
                .build();
    }
}