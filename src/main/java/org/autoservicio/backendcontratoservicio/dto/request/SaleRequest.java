package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleRequest {
    private Long clientId;
    private LocalDate saleDate;
    private String paymentType;
    private BigDecimal taxPercentage;
    private String purchaseOrder;
    private String observations;
    private List<SaleItemRequest> items;
    private List<SalePaymentRequest> payments;
    private List<SaleInstallmentRequest> installments;
}
