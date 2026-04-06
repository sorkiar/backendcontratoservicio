package org.autoservicio.backendcontratoservicio.controller.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.DocumentSeriesResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.DocumentSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/serie-documento")
@RequiredArgsConstructor
public class cDocumentSeries {
    private final DocumentSeriesService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<DocumentSeriesResponse>>>> listar(
            @RequestParam(required = false) String tipoComprobante) {
        Mono<List<DocumentSeriesResponse>> result = (tipoComprobante != null && !tipoComprobante.isBlank())
                ? service.listarPorTipo(tipoComprobante)
                : service.listar();
        return result
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<genericModel<DocumentSeriesResponse>>> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }

    @PostMapping
    public @ResponseBody Mono<ResponseEntity<genericModel<DocumentSeriesResponse>>> crear(
            @RequestParam String tipoComprobante, @RequestParam String serie) {
        return service.crear(tipoComprobante, serie)
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }

    @PatchMapping("/{id}/estado")
    public @ResponseBody Mono<ResponseEntity<genericModel<DocumentSeriesResponse>>> cambiarEstado(
            @PathVariable Long id, @RequestParam Integer status) {
        return service.cambiarEstado(id, status)
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
