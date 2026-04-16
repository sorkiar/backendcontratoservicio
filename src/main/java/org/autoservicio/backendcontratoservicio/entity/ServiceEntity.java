package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.catalog.ChargeUnit;
import org.autoservicio.backendcontratoservicio.entity.catalog.ServiceCategory;

import java.math.BigDecimal;

@Entity
@Table(name = "service")
@Getter
@Setter
public class ServiceEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 20)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id")
    private ServiceCategory serviceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_unit_id")
    private ChargeUnit chargeUnit;

    @Column(name = "price_pen", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePen = BigDecimal.ZERO;

    @Column(name = "estimated_time", length = 50)
    private String estimatedTime;

    @Column(name = "expected_delivery", length = 200)
    private String expectedDelivery;

    @Column(name = "requires_materials")
    private Boolean requiresMaterials = false;

    @Column(name = "requires_specification")
    private Boolean requiresSpecification = false;

    @Column(name = "includes_description", columnDefinition = "TEXT")
    private String includesDescription;

    @Column(name = "excludes_description", columnDefinition = "TEXT")
    private String excludesDescription;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "technical_sheet_url", length = 500)
    private String technicalSheetUrl;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT")
    private Integer status = 1;
}
