package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.catalog.Category;
import org.autoservicio.backendcontratoservicio.entity.catalog.UnitMeasure;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 20)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_measure_id")
    private UnitMeasure unitMeasure;

    @Column(name = "sale_price_pen", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePricePen = BigDecimal.ZERO;

    @Column(name = "estimated_cost_pen", precision = 12, scale = 2)
    private BigDecimal estimatedCostPen = BigDecimal.ZERO;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "technical_spec", columnDefinition = "TEXT")
    private String technicalSpec;

    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;

    @Column(name = "technical_sheet_url", length = 500)
    private String technicalSheetUrl;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT")
    private Integer status = 1;
}
