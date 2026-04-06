package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreditDebitNoteRequest {
    private Long saleId;
    private Long originalDocumentId;
    private String documentTypeCode;
    private Long documentSeriesId;
    private String creditDebitNoteTypeCode;
    private String reason;
    private LocalDate issueDate;
    private List<CreditDebitNoteItemRequest> items;
}
