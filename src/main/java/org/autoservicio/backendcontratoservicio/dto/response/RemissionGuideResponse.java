package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RemissionGuideResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private String series;
    private Integer sequence;
    private LocalDate issueDate;
    private LocalDate transferDate;
    private String transferReason;
    private String transportMode;
    private BigDecimal grossWeight;
    private String weightUnit;
    private Integer packageCount;
    private String originAddress;
    private String destinationAddress;
    private String status;
    private String sunatResponseCode;
    private String sunatMessage;
    private String pdfUrl;
    private String xmlUrl;
    private String cdrUrl;
    private List<RemissionGuideDriverResponse> drivers;
    private List<RemissionGuideItemResponse> items;
}
