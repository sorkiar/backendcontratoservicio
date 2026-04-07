package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.UbigeoMapper;
import org.autoservicio.backendcontratoservicio.dto.response.UbigeoResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.Ubigeo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.UbigeoJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbigeoService {
    private final UbigeoJpaRepo repo;
    private final UbigeoMapper mapper;

    public Mono<List<UbigeoResponse>> listar(Integer status) {
        return Mono.fromCallable(() -> {
            List<Ubigeo> result = (status != null)
                    ? repo.findByStatusOrderByDepartmentAscProvinceAscDistritAsc(status)
                    : repo.findAllByOrderByDepartmentAscProvinceAscDistritAsc();
            return result.stream().map(mapper::toResponse).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
