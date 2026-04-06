package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.DocumentTypeMapper;
import org.autoservicio.backendcontratoservicio.dto.response.DocumentTypeResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.DocumentTypeJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {
    private final DocumentTypeJpaRepo repo;
    private final DocumentTypeMapper mapper;

    public Mono<List<DocumentTypeResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<DocumentTypeResponse>> listarPorPersonType(Long personTypeId) {
        return Mono.fromCallable(() ->
                repo.findByPersonTypeIdAndStatusOrderByNameAsc(personTypeId, 1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
