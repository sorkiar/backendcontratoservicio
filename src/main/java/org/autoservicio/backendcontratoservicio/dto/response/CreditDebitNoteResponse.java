package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreditDebitNoteResponse {
    private Long id;
    private Long saleId;
    private Long originalDocumentId;
    private String originalDocumentNumber;
    private String documentTypeCode;
    private String series;
    private Integer sequence;
    private LocalDate issueDate;
    private String creditDebitNoteTypeCode;
    private String creditDebitNoteTypeName;
    private String reason;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal taxPercentage;
    private String currencyCode;
    private String status;
    private String sunatResponseCode;
    private String sunatMessage;
    private String pdfUrl;
    private String xmlUrl;
    private String cdrUrl;
    private List<CreditDebitNoteItemResponse> items;
}
