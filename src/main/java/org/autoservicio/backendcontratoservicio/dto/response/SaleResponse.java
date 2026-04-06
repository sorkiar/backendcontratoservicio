package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private String clientDocNumber;
    private String saleStatus;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currencyCode;
    private BigDecimal taxPercentage;
    private LocalDate saleDate;
    private String paymentType;
    private String purchaseOrder;
    private String observations;
    private String ticketPdfUrl;
    private List<SaleItemResponse> items;
    private List<SalePaymentResponse> payments;
    private List<SaleDocumentResponse> documents;
}
