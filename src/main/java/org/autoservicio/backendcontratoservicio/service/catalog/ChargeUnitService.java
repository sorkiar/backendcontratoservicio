package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.ChargeUnitMapper;
import org.autoservicio.backendcontratoservicio.dto.response.ChargeUnitResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.ChargeUnitJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargeUnitService {
    private final ChargeUnitJpaRepo repo;
    private final ChargeUnitMapper mapper;

    public Mono<List<ChargeUnitResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
