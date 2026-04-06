package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalePaymentResponse {
    private Long id;
    private Long paymentMethodId;
    private String paymentMethodName;
    private BigDecimal amount;
    private BigDecimal changeAmount;
    private LocalDate paymentDate;
    private String referenceNumber;
    private String proofUrl;
    private String notes;
}
