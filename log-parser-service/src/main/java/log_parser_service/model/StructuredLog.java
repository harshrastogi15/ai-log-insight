package log_parser_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "structured_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String timestamp;
    private String level;
    private String service;
    private String message;

    @Column(name = "raw_log", columnDefinition = "TEXT")
    private String rawLog;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Transient
    @JsonIgnore
    private float[] embedding;

    @PrePersist  // runs automatically just before saving to DB
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}