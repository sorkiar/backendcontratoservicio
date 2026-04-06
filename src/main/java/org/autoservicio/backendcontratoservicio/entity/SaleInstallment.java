package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sale_installment")
@Getter
@Setter
public class SaleInstallment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDIENTE";
}
