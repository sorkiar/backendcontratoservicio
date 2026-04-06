package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class UnitMeasureResponse {
    private Long id;
    private String code;
    private String codeSunat;
    private String name;
    private String symbol;
    private Integer status;
}
