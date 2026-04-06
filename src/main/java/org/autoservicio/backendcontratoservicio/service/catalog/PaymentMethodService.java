package org.autoservicio.backendcontratoservicio.service.catalog;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.PaymentMethodMapper;
import org.autoservicio.backendcontratoservicio.dto.response.PaymentMethodResponse;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.PaymentMethodJpaRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodJpaRepo repo;
    private final PaymentMethodMapper mapper;

    public Mono<List<PaymentMethodResponse>> listar() {
        return Mono.fromCallable(() ->
                repo.findByStatusOrderByNameAsc(1).stream().map(mapper::toResponse).toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
