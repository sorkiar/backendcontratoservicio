package org.autoservicio.backendcontratoservicio.controller;

import reactor.core.publisher.Mono;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.request.ServiceStatusRequest;
import org.autoservicio.backendcontratoservicio.dto.response.ServiceResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.ServiceCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class cService {
  private final ServiceCatalogService service;

  @GetMapping("/siguiente-sku")
  public Mono<ResponseEntity<genericModel<String>>> siguienteSku() {
    return service.siguienteSkuServicio()
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/listar")
  public Mono<ResponseEntity<genericModel<List<ServiceResponse>>>> listar(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) Long serviceCategoryId,
      @RequestParam(required = false) Integer status) {
    return service.listar(name, sku, serviceCategoryId, status)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<genericModel<ServiceResponse>>> obtenerPorId(@PathVariable Long id) {
    return service.obtenerPorId(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping
  public @ResponseBody Mono<ResponseEntity<genericModel<ServiceResponse>>> registrar(
      @RequestBody String dataJson) {
    return service.registrar(dataJson)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PutMapping("/{id}")
  public @ResponseBody Mono<ResponseEntity<genericModel<ServiceResponse>>> actualizar(
      @PathVariable Long id, @RequestBody String dataJson) {
    return service.actualizar(id, dataJson)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PatchMapping("/{id}/estado")
  public @ResponseBody Mono<ResponseEntity<genericModel<ServiceResponse>>> cambiarEstado(
      @PathVariable Long id, @RequestBody ServiceStatusRequest form) {
    return service.cambiarEstado(id, form.getStatus())
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }
}
