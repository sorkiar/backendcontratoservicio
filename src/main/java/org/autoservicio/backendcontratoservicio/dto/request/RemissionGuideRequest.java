package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RemissionGuideRequest {
    private Long documentSeriesId;
    private Long clientId;
    private LocalDate issueDate;
    private LocalDate transferDate;
    private String transferReason;
    private String transferReasonDescription;
    private String transportMode;
    private BigDecimal grossWeight;
    private String weightUnit;
    private Integer packageCount;
    private String originAddress;
    private String originUbigeo;
    private String destinationAddress;
    private String destinationUbigeo;
    private Boolean minorVehicleTransfer;
    private String carrierRuc;
    private String carrierName;
    private String carrierAuthorizationCode;
    private String observations;
    private List<RemissionGuideDriverRequest> drivers;
    private List<RemissionGuideItemRequest> items;
}
