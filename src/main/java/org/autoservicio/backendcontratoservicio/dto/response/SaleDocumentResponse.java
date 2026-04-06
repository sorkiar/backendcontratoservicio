package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SaleDocumentResponse {
    private Long id;
    private String documentTypeCode;
    private String documentTypeName;
    private String series;
    private Integer sequence;
    private LocalDate issueDate;
    private String status;
    private String sunatResponseCode;
    private String sunatMessage;
    private String hashCode;
    private String qrCode;
    private String pdfUrl;
    private String xmlUrl;
    private String cdrUrl;
}
