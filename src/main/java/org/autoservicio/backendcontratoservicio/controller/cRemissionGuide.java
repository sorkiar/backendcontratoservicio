package org.autoservicio.backendcontratoservicio.controller;

import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.request.RemissionGuideRequest;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.RemissionGuideService;
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
@RequestMapping("/api/guias-remision")
@RequiredArgsConstructor
public class cRemissionGuide {
  private final RemissionGuideService service;

  @GetMapping("/listar")
  public Mono<ResponseEntity<genericModel<List<RemissionGuideResponse>>>> listar(
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return service.listar(clientId, status, startDate, endDate)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<genericModel<RemissionGuideResponse>>> obtenerPorId(@PathVariable Long id) {
    return service.obtenerPorId(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping
  public @ResponseBody Mono<ResponseEntity<genericModel<RemissionGuideResponse>>> crear(
      @RequestBody RemissionGuideRequest form) {
    return service.crear(form)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }
}
