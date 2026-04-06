package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalSales;
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private BigDecimal totalDiscount;
    private Long totalDocuments;
    private Long acceptedDocuments;
    private Long pendingDocuments;
    private Long rejectedDocuments;
}
