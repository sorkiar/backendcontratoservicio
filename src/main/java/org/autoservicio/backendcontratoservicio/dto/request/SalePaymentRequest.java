package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalePaymentRequest {
    private Long paymentMethodId;
    private BigDecimal amount;
    private BigDecimal changeAmount;
    private LocalDate paymentDate;
    private String referenceNumber;
    private String notes;
}
