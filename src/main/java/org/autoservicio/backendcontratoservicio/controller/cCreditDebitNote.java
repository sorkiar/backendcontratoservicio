package org.autoservicio.backendcontratoservicio.controller;

import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.request.CreditDebitNoteRequest;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.CreditDebitNoteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
public class cCreditDebitNote {
  private final CreditDebitNoteService service;

  @GetMapping("/listar")
  public Mono<ResponseEntity<genericModel<List<CreditDebitNoteResponse>>>> listar(
      @RequestParam(required = false) Long saleId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return service.listar(saleId, status, startDate, endDate)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<genericModel<CreditDebitNoteResponse>>> obtenerPorId(@PathVariable Long id) {
    return service.obtenerPorId(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping
  public @ResponseBody Mono<ResponseEntity<genericModel<CreditDebitNoteResponse>>> crear(
      @RequestBody CreditDebitNoteRequest form) {
    return service.crear(form)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }
}
