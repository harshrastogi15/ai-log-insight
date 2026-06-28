package log_parser_service.repository;

import log_parser_service.model.StructuredLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StructuredLogRepository extends JpaRepository<StructuredLog, Long> {

    List<StructuredLog> findByLevel(String level);
    List<StructuredLog> findByService(String service);
    List<StructuredLog> findTop10ByOrderByCreatedAtDesc();

    List<StructuredLog> findByLevelAndService(String upperCase, String service);

    Long countByLevel(String error);

    List<StructuredLog> findTop100ByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query(value = "UPDATE structured_logs SET embedding = CAST(:vector AS vector) WHERE id = :id",
            nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("vector") String vector);

    @Query(value = """
        SELECT *
        FROM structured_logs
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<StructuredLog> findSimilarLogs(
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );

    @Query(value = """
        SELECT *
        FROM structured_logs
        WHERE (:level IS NULL OR level = :level)
          AND (:service IS NULL OR service = :service)
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<StructuredLog> findSimilarLogsFiltered(
            @Param("queryVector") String queryVector,
            @Param("level") String level,
            @Param("service") String service,
            @Param("topK") int topK
    );

}