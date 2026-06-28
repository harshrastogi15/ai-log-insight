package log_parser_service.service;

import log_parser_service.model.StructuredLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogParserServiceTest {

    private LogParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new LogParserService();
    }

    @Test
    void shouldParseErrorLog() {
        String raw = "2026-05-28 ERROR payment-service Database timeout";
        StructuredLog log = parserService.parse(raw);

        assertEquals("ERROR", log.getLevel());
        assertEquals("payment-service", log.getService());
        assertEquals("Database timeout", log.getMessage());
    }

    @Test
    void shouldParseLogWithTime() {
        String raw = "2024-03-15 09:12:03 WARN auth-service Login failed";
        StructuredLog log = parserService.parse(raw);

        assertEquals("WARN", log.getLevel());
        assertEquals("auth-service", log.getService());
        assertEquals("Login failed", log.getMessage());
    }

    @Test
    void shouldParseLogWithBrackets() {
        String raw = "2024-03-15 09:12:03 ERROR [auth-service] Login failed";
        StructuredLog log = parserService.parse(raw);

        assertEquals("ERROR", log.getLevel());
        assertEquals("auth-service", log.getService());
    }

    @Test
    void shouldHandleAllLogLevels() {
        assertDoesNotThrow(() -> parserService.parse("2026-05-28 DEBUG svc msg"));
        assertDoesNotThrow(() -> parserService.parse("2026-05-28 INFO svc msg"));
        assertDoesNotThrow(() -> parserService.parse("2026-05-28 WARN svc msg"));
        assertDoesNotThrow(() -> parserService.parse("2026-05-28 ERROR svc msg"));
        assertDoesNotThrow(() -> parserService.parse("2026-05-28 FATAL svc msg"));
    }

    @Test
    void shouldReturnUnknownForInvalidLog() {
        StructuredLog log = parserService.parse("this is not a valid log");
        assertEquals("UNKNOWN", log.getLevel());
        assertEquals("unknown", log.getService());
    }

    @Test
    void shouldThrowForBlankLog() {
        assertThrows(IllegalArgumentException.class, () -> parserService.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parserService.parse(null));
    }
}