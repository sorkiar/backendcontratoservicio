package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class DocumentTypeResponse {
    private Long id;
    private Long personTypeId;
    private String personTypeName;
    private String name;
    private Integer length;
    private String description;
    private Integer status;
}
