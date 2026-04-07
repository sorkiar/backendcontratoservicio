package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.response.SalesReportResponse;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleJpaRepo;
import org.autoservicio.backendcontratoservicio.specification.SaleSpec;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
  private final SaleJpaRepo saleRepo;
  private final SaleDocumentJpaRepo saleDocumentRepo;

  public Mono<SalesReportResponse> reporteVentas(LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(() -> {
      var spec = SaleSpec.build(null, null, startDate, endDate, null);
      var sales = saleRepo.findAll(spec);

      SalesReportResponse report = new SalesReportResponse();
      report.setStartDate(startDate);
      report.setEndDate(endDate);
      report.setTotalSales((long) sales.size());
      report.setTotalRevenue(sales.stream()
          .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::add));
      report.setTotalTax(sales.stream()
          .map(s -> s.getTaxAmount() != null ? s.getTaxAmount() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::add));
      report.setTotalDiscount(sales.stream()
          .map(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::add));

      var docs = saleDocumentRepo.findAll();
      report.setTotalDocuments((long) docs.size());
      report.setAcceptedDocuments(docs.stream().filter(d -> "ACEPTADO".equals(d.getStatus())).count());
      report.setPendingDocuments(docs.stream().filter(d -> "PENDIENTE".equals(d.getStatus())).count());
      report.setRejectedDocuments(docs.stream()
          .filter(d -> "RECHAZADO".equals(d.getStatus()) || "ERROR".equals(d.getStatus())).count());

      return report;
    }).subscribeOn(Schedulers.boundedElastic());
  }
}
