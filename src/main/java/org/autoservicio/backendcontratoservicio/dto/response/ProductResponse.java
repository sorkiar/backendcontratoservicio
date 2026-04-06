package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long unitMeasureId;
    private String unitMeasureName;
    private BigDecimal salePricePen;
    private BigDecimal estimatedCostPen;
    private String brand;
    private String model;
    private String shortDescription;
    private String technicalSpec;
    private String mainImageUrl;
    private String technicalSheetUrl;
    private Integer status;
}
