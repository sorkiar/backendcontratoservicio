package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class PaymentMethodResponse {
    private Long id;
    private String code;
    private String name;
    private Boolean requiresProof;
    private Integer status;
}
