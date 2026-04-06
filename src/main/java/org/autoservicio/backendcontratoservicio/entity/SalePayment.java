package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.catalog.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sale_payment")
@Getter
@Setter
public class SalePayment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "change_amount", precision = 12, scale = 2)
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "proof_url", length = 500)
    private String proofUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
