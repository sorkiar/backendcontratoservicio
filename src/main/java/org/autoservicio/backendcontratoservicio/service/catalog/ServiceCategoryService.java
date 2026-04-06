package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.ServiceCategoryMapper;
import org.autoservicio.backendcontratoservicio.dto.response.ServiceCategoryResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.ServiceCategoryJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {
    private final ServiceCategoryJpaRepo repo;
    private final ServiceCategoryMapper mapper;

    public Mono<List<ServiceCategoryResponse>> listar(Long id, Integer estado) {
        return Mono.fromCallable(() -> {
            if (id != null) {
                int status = (estado != null) ? estado : 1;
                return repo.findByIdAndStatus(id, status)
                        .map(mapper::toResponse)
                        .map(List::of)
                        .orElse(List.of());
            }
            int status = (estado != null) ? estado : 1;
            return repo.findByStatusOrderByNameAsc(status).stream().map(mapper::toResponse).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
