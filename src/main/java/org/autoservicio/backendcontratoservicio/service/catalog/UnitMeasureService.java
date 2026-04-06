package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.UnitMeasureMapper;
import org.autoservicio.backendcontratoservicio.dto.response.UnitMeasureResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.UnitMeasureJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitMeasureService {
    private final UnitMeasureJpaRepo repo;
    private final UnitMeasureMapper mapper;

    public Mono<List<UnitMeasureResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
