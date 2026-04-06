package org.autoservicio.backendcontratoservicio.controller.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.ServiceCategoryResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.catalog.ServiceCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categoria-servicio")
@RequiredArgsConstructor
public class cServiceCategory {
    private final ServiceCategoryService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<ServiceCategoryResponse>>>> listar(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Integer estado) {
        return service.listar(id, estado)
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
