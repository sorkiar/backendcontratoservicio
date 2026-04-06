package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class ChargeUnitResponse {
    private Long id;
    private String code;
    private String name;
    private Integer status;
}
