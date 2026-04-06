package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "remission_guide_driver")
@Getter
@Setter
public class RemissionGuideDriver extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remission_guide_id", nullable = false)
    private RemissionGuide remissionGuide;

    @Column(name = "driver_first_name", length = 100)
    private String driverFirstName;

    @Column(name = "driver_last_name", length = 100)
    private String driverLastName;

    @Column(name = "driver_doc_type", length = 5)
    private String driverDocType;

    @Column(name = "driver_doc_number", length = 20)
    private String driverDocNumber;

    @Column(name = "driver_license_number", length = 30)
    private String driverLicenseNumber;

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;
}
