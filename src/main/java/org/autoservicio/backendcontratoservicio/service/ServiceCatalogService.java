package org.autoservicio.backendcontratoservicio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.ServiceEntityMapper;
import org.autoservicio.backendcontratoservicio.dto.request.ServiceRequest;
import org.autoservicio.backendcontratoservicio.dto.response.ServiceResponse;
import org.autoservicio.backendcontratoservicio.entity.ServiceEntity;
import org.autoservicio.backendcontratoservicio.entity.catalog.ChargeUnit;
import org.autoservicio.backendcontratoservicio.entity.catalog.ServiceCategory;
import org.autoservicio.backendcontratoservicio.jparepository.ServiceEntityJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.ChargeUnitJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.ServiceCategoryJpaRepo;
import org.autoservicio.backendcontratoservicio.specification.ServiceEntitySpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {
  private final ServiceEntityJpaRepo repo;
  private final ServiceEntityMapper mapper;
  private final ServiceCategoryJpaRepo serviceCategoryRepo;
  private final ChargeUnitJpaRepo chargeUnitRepo;
  private final SkuSequenceService skuSequenceService;
  private final ObjectMapper objectMapper;
  private final PlatformTransactionManager txManager;

  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  public Mono<String> siguienteSkuServicio() {
    return Mono.fromCallable(() -> skuSequenceService.peekNextSku("SRV"))
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<List<ServiceResponse>> listar(String name, String sku, Long serviceCategoryId, Integer status) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      Specification<ServiceEntity> spec = ServiceEntitySpec.build(name, sku, serviceCategoryId, status);
      return repo.findAll(spec).stream().map(mapper::toResponse).toList();
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ServiceResponse> obtenerPorId(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      ServiceEntity s = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id));
      return mapper.toResponse(s);
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ServiceResponse> registrar(String dataJson) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      ServiceRequest request;
      try {
        request = objectMapper.readValue(dataJson, ServiceRequest.class);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      ServiceEntity se = new ServiceEntity();
      se.setSku(skuSequenceService.nextSku("SRV"));
      se.setName(request.getName());
      se.setStatus(1);
      if (request.getServiceCategoryId() != null) {
        ServiceCategory sc = serviceCategoryRepo.findById(request.getServiceCategoryId()).orElse(null);
        se.setServiceCategory(sc);
      }
      if (request.getChargeUnitId() != null) {
        ChargeUnit cu = chargeUnitRepo.findById(request.getChargeUnitId()).orElse(null);
        se.setChargeUnit(cu);
      }
      se.setPricePen(request.getPricePen());
      se.setEstimatedTime(request.getEstimatedTime());
      se.setExpectedDelivery(request.getExpectedDelivery());
      se.setRequiresMaterials(request.getRequiresMaterials());
      se.setRequiresSpecification(request.getRequiresSpecification());
      se.setIncludesDescription(request.getIncludesDescription());
      se.setExcludesDescription(request.getExcludesDescription());
      se.setConditions(request.getConditions());
      se.setShortDescription(request.getShortDescription());
      se.setDetailedDescription(request.getDetailedDescription());
      return mapper.toResponse(repo.save(se));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ServiceResponse> actualizar(Long id, String dataJson) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      ServiceRequest request;
      try {
        request = objectMapper.readValue(dataJson, ServiceRequest.class);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      ServiceEntity se = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id));
      se.setName(request.getName());
      if (request.getServiceCategoryId() != null) {
        ServiceCategory sc = serviceCategoryRepo.findById(request.getServiceCategoryId()).orElse(null);
        se.setServiceCategory(sc);
      }
      if (request.getChargeUnitId() != null) {
        ChargeUnit cu = chargeUnitRepo.findById(request.getChargeUnitId()).orElse(null);
        se.setChargeUnit(cu);
      }
      se.setPricePen(request.getPricePen());
      se.setEstimatedTime(request.getEstimatedTime());
      se.setExpectedDelivery(request.getExpectedDelivery());
      se.setRequiresMaterials(request.getRequiresMaterials());
      se.setRequiresSpecification(request.getRequiresSpecification());
      se.setIncludesDescription(request.getIncludesDescription());
      se.setExcludesDescription(request.getExcludesDescription());
      se.setConditions(request.getConditions());
      se.setShortDescription(request.getShortDescription());
      se.setDetailedDescription(request.getDetailedDescription());
      return mapper.toResponse(repo.save(se));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ServiceResponse> cambiarEstado(Long id, Integer status) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      ServiceEntity se = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id));
      se.setStatus(status);
      return mapper.toResponse(repo.save(se));
    })).subscribeOn(Schedulers.boundedElastic());
  }
}
