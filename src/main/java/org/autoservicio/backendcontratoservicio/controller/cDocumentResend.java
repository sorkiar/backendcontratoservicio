package org.autoservicio.backendcontratoservicio.controller;

import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteResponse;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideResponse;
import org.autoservicio.backendcontratoservicio.dto.response.SaleDocumentResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.job.SunatDocumentJobService;
import org.autoservicio.backendcontratoservicio.service.DocumentResendService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class cDocumentResend {
  private final DocumentResendService service;
  private final SunatDocumentJobService jobService;

  @GetMapping("/sunat/listar")
  public Mono<ResponseEntity<genericModel<List<SaleDocumentResponse>>>> listar(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String tipoComprobante,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return service.listarComprobantes(status, tipoComprobante, startDate, endDate)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/documentos/{id}")
  public Mono<ResponseEntity<genericModel<SaleDocumentResponse>>> obtenerDocumento(@PathVariable Long id) {
    return service.obtenerDocumento(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping("/documentos/{id}/reenviar")
  public @ResponseBody Mono<ResponseEntity<genericModel<SaleDocumentResponse>>> reenviarDocumento(
      @PathVariable Long id) {
    return service.reenviarDocumento(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping("/notas/{id}/reenviar")
  public @ResponseBody Mono<ResponseEntity<genericModel<CreditDebitNoteResponse>>> reenviarNota(
      @PathVariable Long id) {
    return service.reenviarNota(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping("/guias/{id}/reenviar")
  public @ResponseBody Mono<ResponseEntity<genericModel<RemissionGuideResponse>>> reenviarGuia(
      @PathVariable Long id) {
    return service.reenviarGuia(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping("/admin/trigger-job")
  public @ResponseBody Mono<ResponseEntity<genericModel<String>>> triggerJob() {
    return Mono.fromCallable(() -> {
          jobService.scheduledTick();
          return "Job ejecutado manualmente";
        }).flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Job disparado manualmente"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }
}
