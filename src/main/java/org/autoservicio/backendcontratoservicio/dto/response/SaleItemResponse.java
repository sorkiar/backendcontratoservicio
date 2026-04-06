package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleItemResponse {
    private Long id;
    private String itemType;
    private Long productId;
    private Long serviceId;
    private String sku;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Long unitMeasureId;
    private String unitMeasureName;
}
