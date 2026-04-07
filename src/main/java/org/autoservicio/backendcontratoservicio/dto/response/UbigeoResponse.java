package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class UbigeoResponse {
    private String ubigeo;
    private String department;
    private String province;
    private String distrit;
    private Integer status;
}
