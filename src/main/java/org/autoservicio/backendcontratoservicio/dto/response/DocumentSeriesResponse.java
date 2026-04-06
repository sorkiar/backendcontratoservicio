package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class DocumentSeriesResponse {
    private Long id;
    private String documentTypeSunatCode;
    private String documentTypeSunatName;
    private String series;
    private Integer currentSequence;
    private Integer status;
}
