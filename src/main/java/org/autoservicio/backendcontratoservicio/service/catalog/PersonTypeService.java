package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.PersonTypeMapper;
import org.autoservicio.backendcontratoservicio.dto.response.PersonTypeResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.PersonTypeJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonTypeService {
    private final PersonTypeJpaRepo repo;
    private final PersonTypeMapper mapper;

    public Mono<List<PersonTypeResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
