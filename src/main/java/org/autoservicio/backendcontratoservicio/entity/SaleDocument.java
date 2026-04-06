package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;

import java.time.LocalDate;

@Entity
@Table(name = "sale_document")
@Getter
@Setter
public class SaleDocument extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_sunat_code", nullable = false)
    private DocumentTypeSunat documentTypeSunat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_series_id")
    private DocumentSeries documentSeries;

    @Column(name = "series", length = 10)
    private String series;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDIENTE";

    @Column(name = "sunat_response_code", length = 10)
    private String sunatResponseCode;

    @Column(name = "sunat_message", length = 500)
    private String sunatMessage;

    @Column(name = "hash_code", length = 500)
    private String hashCode;

    @Column(name = "qr_code", length = 1000)
    private String qrCode;

    @Column(name = "xml_base64", columnDefinition = "LONGTEXT")
    private String xmlBase64;

    @Column(name = "cdr_base64", columnDefinition = "LONGTEXT")
    private String cdrBase64;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "xml_url", length = 500)
    private String xmlUrl;

    @Column(name = "cdr_url", length = 500)
    private String cdrUrl;
}
