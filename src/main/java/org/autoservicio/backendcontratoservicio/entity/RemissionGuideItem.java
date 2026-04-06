package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "remission_guide_item")
@Getter
@Setter
public class RemissionGuideItem extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remission_guide_id", nullable = false)
    private RemissionGuide remissionGuide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_measure_sunat", length = 10)
    private String unitMeasureSunat;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "subtotal_amount", precision = 12, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
