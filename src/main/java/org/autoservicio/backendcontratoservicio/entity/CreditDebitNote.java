package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.catalog.CreditDebitNoteType;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "credit_debit_note")
@Getter
@Setter
public class CreditDebitNote extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_document_id", nullable = false)
    private SaleDocument originalDocument;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_debit_note_type_code", nullable = false)
    private CreditDebitNoteType creditDebitNoteType;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage = new BigDecimal("18.00");

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "PEN";

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

    @OneToMany(mappedBy = "creditDebitNote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreditDebitNoteItem> items = new ArrayList<>();
}
