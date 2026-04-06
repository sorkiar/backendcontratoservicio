package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceResponse {
    private Long id;
    private String sku;
    private String name;
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private Long chargeUnitId;
    private String chargeUnitName;
    private BigDecimal pricePen;
    private String estimatedTime;
    private String expectedDelivery;
    private Boolean requiresMaterials;
    private Boolean requiresSpecification;
    private String includesDescription;
    private String excludesDescription;
    private String conditions;
    private String shortDescription;
    private String detailedDescription;
    private String imageUrl;
    private String technicalSheetUrl;
    private Integer status;
}
