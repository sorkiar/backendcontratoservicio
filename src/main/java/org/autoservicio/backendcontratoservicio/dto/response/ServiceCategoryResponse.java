package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class ServiceCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Integer status;
}
