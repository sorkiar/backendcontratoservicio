package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.CreditDebitNoteTypeMapper;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteTypeResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.CreditDebitNoteTypeJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditDebitNoteTypeService {
    private final CreditDebitNoteTypeJpaRepo repo;
    private final CreditDebitNoteTypeMapper mapper;

    public Mono<List<CreditDebitNoteTypeResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<CreditDebitNoteTypeResponse>> listarPorCategoria(String noteCategory) {
        return Mono.fromCallable(() ->
                repo.findByNoteCategoryAndStatusOrderByNameAsc(noteCategory, 1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
