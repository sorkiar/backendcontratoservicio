package org.autoservicio.backendcontratoservicio.controller;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.SalesReportResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.PdfReportService;
import org.autoservicio.backendcontratoservicio.service.ReportService;
import org.autoservicio.backendcontratoservicio.service.TicketPdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class cReport {
  private final ReportService service;
  private final PdfReportService pdfService;
  private final TicketPdfService ticketPdfService;

  @GetMapping("/ventas")
  public Mono<ResponseEntity<genericModel<SalesReportResponse>>> reporteVentas(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return service.reporteVentas(startDate, endDate)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping(value = "/pdf/venta/{saleId}/documento/{documentId}", produces = "application/pdf")
  public Mono<ResponseEntity<byte[]>> pdfVenta(
      @PathVariable Long saleId,
      @PathVariable Long documentId) {
    return pdfService.generarPdfVenta(saleId, documentId)
        .map(bytes -> ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"venta-" + saleId + "-doc-" + documentId + ".pdf\"")
            .body(bytes))
        .doOnSuccess(r -> log.info("PDF venta generado: saleId={}, documentId={}", saleId, documentId))
        .doOnError(e -> log.error("Error generando PDF venta {}/{}: {}", saleId, documentId, e.getMessage()))
        .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().<byte[]>build()));
  }

  @GetMapping(value = "/pdf/nota/{noteId}", produces = "application/pdf")
  public Mono<ResponseEntity<byte[]>> pdfNota(@PathVariable Long noteId) {
    return pdfService.generarPdfNotaCreditoDebito(noteId)
        .map(bytes -> ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"nota-credito-debito-" + noteId + ".pdf\"")
            .body(bytes))
        .doOnSuccess(r -> log.info("PDF nota generado: noteId={}", noteId))
        .doOnError(e -> log.error("Error generando PDF nota {}: {}", noteId, e.getMessage()))
        .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().<byte[]>build()));
  }

  @GetMapping(value = "/pdf/guia/{guideId}", produces = "application/pdf")
  public Mono<ResponseEntity<byte[]>> pdfGuia(@PathVariable Long guideId) {
    return pdfService.generarPdfGuiaRemision(guideId)
        .map(bytes -> ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"guia-remision-" + guideId + ".pdf\"")
            .body(bytes))
        .doOnSuccess(r -> log.info("PDF guía generado: guideId={}", guideId))
        .doOnError(e -> log.error("Error generando PDF guía {}: {}", guideId, e.getMessage()))
        .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().<byte[]>build()));
  }

  @GetMapping(value = "/pdf/ticket/{saleId}/{documentId}", produces = "application/pdf")
  public Mono<ResponseEntity<byte[]>> pdfTicket(
      @PathVariable Long saleId,
      @PathVariable Long documentId) {
    return Mono.fromCallable(() -> ticketPdfService.generateForSale(saleId, documentId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(bytes -> ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"ticket-" + saleId + ".pdf\"")
            .body(bytes))
        .doOnSuccess(r -> log.info("Ticket PDF generado: saleId={}, documentId={}", saleId, documentId))
        .doOnError(e -> log.error("Error generando ticket PDF {}/{}: {}", saleId, documentId, e.getMessage()))
        .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().<byte[]>build()));
  }
}
