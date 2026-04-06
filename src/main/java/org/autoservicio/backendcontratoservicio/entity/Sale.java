package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale")
@Getter
@Setter
public class Sale extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "sale_status", nullable = false, length = 20)
    private String saleStatus = "PENDIENTE";

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "PEN";

    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage = new BigDecimal("18.00");

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType = "CONTADO";

    @Column(name = "purchase_order", length = 100)
    private String purchaseOrder;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalePayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleInstallment> installments = new ArrayList<>();

    @Column(name = "ticket_pdf_url", length = 500)
    private String ticketPdfUrl;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleDocument> documents = new ArrayList<>();
}
