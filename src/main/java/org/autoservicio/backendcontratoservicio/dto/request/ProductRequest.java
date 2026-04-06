package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private Long categoryId;
    private Long unitMeasureId;
    private BigDecimal salePricePen;
    private BigDecimal estimatedCostPen;
    private String brand;
    private String model;
    private String shortDescription;
    private String technicalSpec;
}
