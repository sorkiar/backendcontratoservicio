package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class CreditDebitNoteTypeResponse {
    private String code;
    private String name;
    private String noteCategory;
    private Integer status;
}
