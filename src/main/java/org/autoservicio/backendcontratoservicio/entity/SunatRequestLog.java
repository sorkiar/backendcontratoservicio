package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sunat_request_log")
@Getter
@Setter
public class SunatRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", length = 30)
    private String documentType;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "success", nullable = false)
    private Boolean success = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
