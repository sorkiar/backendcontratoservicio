package org.autoservicio.backendcontratoservicio.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SaleInstallmentRequest {
    private BigDecimal amount;
    private LocalDate dueDate;
}
