package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;

@Data
public class RemissionGuideDriverRequest {
    private String driverFirstName;
    private String driverLastName;
    private String driverDocType;
    private String driverDocNumber;
    private String driverLicenseNumber;
    private String vehiclePlate;
}
