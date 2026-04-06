package org.autoservicio.backendcontratoservicio.controller.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteTypeResponse;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.service.catalog.CreditDebitNoteTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tipo-nota")
@RequiredArgsConstructor
public class cCreditDebitNoteType {
    private final CreditDebitNoteTypeService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<CreditDebitNoteTypeResponse>>>> listar(
            @RequestParam(required = false) String categoria) {
        Mono<List<CreditDebitNoteTypeResponse>> result = (categoria != null && !categoria.isBlank())
                ? service.listarPorCategoria(categoria)
                : service.listar();
        return result
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
