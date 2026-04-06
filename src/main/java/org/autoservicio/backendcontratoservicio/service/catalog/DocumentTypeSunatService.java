package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.DocumentTypeSunatMapper;
import org.autoservicio.backendcontratoservicio.dto.response.DocumentTypeSunatResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.DocumentTypeSunatJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeSunatService {
    private final DocumentTypeSunatJpaRepo repo;
    private final DocumentTypeSunatMapper mapper;

    public Mono<List<DocumentTypeSunatResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
