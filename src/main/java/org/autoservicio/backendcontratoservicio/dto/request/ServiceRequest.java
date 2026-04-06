package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceRequest {
    private String name;
    private Long serviceCategoryId;
    private Long chargeUnitId;
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
}
