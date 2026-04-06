package org.autoservicio.backendcontratoservicio.controller.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.DocumentTypeResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.catalog.DocumentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tipo-documento")
@RequiredArgsConstructor
public class cDocumentType {
    private final DocumentTypeService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<DocumentTypeResponse>>>> listar(
            @RequestParam(required = false) Long personTypeId) {
        Mono<List<DocumentTypeResponse>> result = (personTypeId != null)
                ? service.listarPorPersonType(personTypeId)
                : service.listar();
        return result
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
