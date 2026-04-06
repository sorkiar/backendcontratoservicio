package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RemissionGuideItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal quantity;
    private String unitMeasureSunat;
    private BigDecimal unitPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
}
