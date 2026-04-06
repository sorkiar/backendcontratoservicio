package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;

@Data
public class RemissionGuideDriverResponse {
    private Long id;
    private String driverFirstName;
    private String driverLastName;
    private String driverDocType;
    private String driverDocNumber;
    private String driverLicenseNumber;
    private String vehiclePlate;
}
