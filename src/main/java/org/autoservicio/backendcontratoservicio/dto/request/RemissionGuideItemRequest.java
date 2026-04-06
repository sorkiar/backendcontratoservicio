package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RemissionGuideItemRequest {
    private Long productId;
    private String description;
    private BigDecimal quantity;
    private String unitMeasureSunat;
    private BigDecimal unitPrice;
}
