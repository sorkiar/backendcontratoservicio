package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditDebitNoteItemRequest {
    private String itemType;
    private Long productId;
    private Long serviceId;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private Long unitMeasureId;
}
