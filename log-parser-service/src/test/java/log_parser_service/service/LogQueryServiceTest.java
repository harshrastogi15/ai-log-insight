package log_parser_service.service;

import log_parser_service.dto.LogQueryRequest;
import log_parser_service.dto.LogQueryResponse;
import log_parser_service.model.StructuredLog;
import log_parser_service.repository.StructuredLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // Mockito creates mocks automatically
class LogQueryServiceTest {

    @Mock private EmbeddingClient embeddingClient;
    @Mock private LlmClient llmClient;
    @Mock private StructuredLogRepository repository;

    private LogQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new LogQueryService(embeddingClient, llmClient, repository);
        ReflectionTestUtils.setField(queryService, "topK", 15);
    }

    @Test
    void shouldReturnAiAnswerWhenLogsFound() {
        // ARRANGE — set up fake responses
        float[] fakeVector = new float[384];
        when(embeddingClient.embed(anyString())).thenReturn(fakeVector);
        when(embeddingClient.toVectorLiteral(any())).thenReturn("[0.1,0.2]");

        StructuredLog log = StructuredLog.builder()
                .id(1L).level("ERROR")
                .service("payment-service")
                .message("DB timeout")
                .timestamp("2026-05-28")
                .build();

        when(repository.findSimilarLogsFiltered(any(), any(), any(), anyInt()))
                .thenReturn(List.of(log));

        when(llmClient.answer(anyString(), anyString()))
                .thenReturn("payment-service has DB timeouts");

        // ACT
        LogQueryRequest request = new LogQueryRequest();
        request.setQuestion("What is wrong?");
        LogQueryResponse response = queryService.query(request);

        // ASSERT
        assertEquals("What is wrong?", response.getQuestion());
        assertEquals("payment-service has DB timeouts", response.getAnswer());
        assertEquals(1, response.getLogsAnalyzed());
    }

    @Test
    void shouldReturnNoLogsMessageWhenEmpty() {
        float[] fakeVector = new float[384];
        when(embeddingClient.embed(anyString())).thenReturn(fakeVector);
        when(embeddingClient.toVectorLiteral(any())).thenReturn("[0.1,0.2]");
        when(repository.findSimilarLogsFiltered(any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        LogQueryRequest request = new LogQueryRequest();
        request.setQuestion("anything");
        LogQueryResponse response = queryService.query(request);

        assertEquals("No logs found matching your query.", response.getAnswer());
        assertEquals(0, response.getLogsAnalyzed());

        // LLM should never be called if no logs found
        verify(llmClient, never()).answer(any(), any());
    }

    @Test
    void shouldPassLevelFilterToRepository() {
        float[] fakeVector = new float[384];
        when(embeddingClient.embed(anyString())).thenReturn(fakeVector);
        when(embeddingClient.toVectorLiteral(any())).thenReturn("[0.1]");
        when(repository.findSimilarLogsFiltered(any(), eq("ERROR"), any(), anyInt()))
                .thenReturn(List.of());

        LogQueryRequest request = new LogQueryRequest();
        request.setQuestion("errors only");
        request.setLevel("ERROR");
        queryService.query(request);

        // verify ERROR was passed to repository
        verify(repository).findSimilarLogsFiltered(any(), eq("ERROR"), isNull(), anyInt());
    }
}