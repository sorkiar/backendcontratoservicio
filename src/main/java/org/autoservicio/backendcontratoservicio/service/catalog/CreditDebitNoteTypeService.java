package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.CreditDebitNoteTypeMapper;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteTypeResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.CreditDebitNoteType;
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

    public Mono<List<CreditDebitNoteTypeResponse>> listar(Integer status) {
        return Mono.fromCallable(() -> {
            List<CreditDebitNoteType> result = (status != null)
                    ? repo.findByStatusOrderByNameAsc(status)
                    : repo.findAllByOrderByNameAsc();
            return result.stream().map(mapper::toResponse).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<CreditDebitNoteTypeResponse>> listarPorCategoria(String noteCategory, Integer status) {
        return Mono.fromCallable(() -> {
            List<CreditDebitNoteType> result = (status != null)
                    ? repo.findByNoteCategoryAndStatusOrderByNameAsc(noteCategory, status)
                    : repo.findByNoteCategoryOrderByNameAsc(noteCategory);
            return result.stream().map(mapper::toResponse).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
