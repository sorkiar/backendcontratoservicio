package org.autoservicio.backendcontratoservicio.controller.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.DocumentTypeSunatResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.catalog.DocumentTypeSunatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tipo-comprobante-sunat")
@RequiredArgsConstructor
public class cDocumentTypeSunat {
    private final DocumentTypeSunatService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<DocumentTypeSunatResponse>>>> listar() {
        return service.listar()
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
