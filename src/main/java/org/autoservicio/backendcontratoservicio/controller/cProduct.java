package org.autoservicio.backendcontratoservicio.controller;

import reactor.core.publisher.Mono;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.request.ProductStatusRequest;
import org.autoservicio.backendcontratoservicio.dto.response.ProductResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class cProduct {
  private final ProductService service;

  @GetMapping("/siguiente-sku")
  public Mono<ResponseEntity<genericModel<String>>> siguienteSku() {
    return service.siguienteSkuProducto()
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/listar")
  public Mono<ResponseEntity<genericModel<List<ProductResponse>>>> listar(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Integer status) {
    return service.listar(name, sku, categoryId, status)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<genericModel<ProductResponse>>> obtenerPorId(@PathVariable Long id) {
    return service.obtenerPorId(id)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public @ResponseBody Mono<ResponseEntity<genericModel<ProductResponse>>> registrar(
      @RequestPart("data") String dataJson,
      @RequestPart(value = "mainImage", required = false) Mono<FilePart> mainImage) {
    return service.registrar(dataJson, mainImage)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public @ResponseBody Mono<ResponseEntity<genericModel<ProductResponse>>> actualizar(
      @PathVariable Long id, @RequestPart("data") String dataJson) {
    return service.actualizar(id, dataJson)
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }

  @PatchMapping("/{id}/estado")
  public @ResponseBody Mono<ResponseEntity<genericModel<ProductResponse>>> cambiarEstado(
      @PathVariable Long id, @RequestBody ProductStatusRequest form) {
    return service.cambiarEstado(id, form.getStatus())
        .flatMap(GenericoException::success)
        .doOnSuccess(r -> log.info("Operación exitosa"))
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .onErrorResume(GenericoException::error);
  }
}
