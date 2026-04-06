package org.autoservicio.backendcontratoservicio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.ProductMapper;
import org.autoservicio.backendcontratoservicio.dto.request.ProductRequest;
import org.autoservicio.backendcontratoservicio.dto.response.ProductResponse;
import org.autoservicio.backendcontratoservicio.entity.Product;
import org.autoservicio.backendcontratoservicio.entity.catalog.Category;
import org.autoservicio.backendcontratoservicio.entity.catalog.UnitMeasure;
import org.autoservicio.backendcontratoservicio.jparepository.ProductJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.CategoryJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.UnitMeasureJpaRepo;
import org.autoservicio.backendcontratoservicio.specification.ProductSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductJpaRepo repo;
  private final ProductMapper mapper;
  private final CategoryJpaRepo categoryRepo;
  private final UnitMeasureJpaRepo unitMeasureRepo;
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

  public Mono<String> siguienteSkuProducto() {
    return Mono.fromCallable(() -> skuSequenceService.peekNextSku("PRD"))
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<List<ProductResponse>> listar(String name, String sku, Long categoryId, Integer status) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      Specification<Product> spec = ProductSpec.build(name, sku, categoryId, status);
      return repo.findAll(spec).stream().map(mapper::toResponse).toList();
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ProductResponse> obtenerPorId(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      Product p = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
      return mapper.toResponse(p);
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ProductResponse> registrar(String dataJson, Mono<FilePart> mainImage) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      ProductRequest request;
      try {
        request = objectMapper.readValue(dataJson, ProductRequest.class);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      Product product = new Product();
      product.setSku(skuSequenceService.nextSku("PRD"));
      product.setName(request.getName());
      product.setStatus(1);
      if (request.getCategoryId() != null) {
        Category cat = categoryRepo.findById(request.getCategoryId()).orElse(null);
        product.setCategory(cat);
      }
      if (request.getUnitMeasureId() != null) {
        UnitMeasure um = unitMeasureRepo.findById(request.getUnitMeasureId()).orElse(null);
        product.setUnitMeasure(um);
      }
      product.setSalePricePen(request.getSalePricePen());
      product.setEstimatedCostPen(request.getEstimatedCostPen());
      product.setBrand(request.getBrand());
      product.setModel(request.getModel());
      product.setShortDescription(request.getShortDescription());
      product.setTechnicalSpec(request.getTechnicalSpec());
      return mapper.toResponse(repo.save(product));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ProductResponse> actualizar(Long id, String dataJson) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      ProductRequest request;
      try {
        request = objectMapper.readValue(dataJson, ProductRequest.class);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      Product product = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
      product.setName(request.getName());
      if (request.getCategoryId() != null) {
        Category cat = categoryRepo.findById(request.getCategoryId()).orElse(null);
        product.setCategory(cat);
      }
      if (request.getUnitMeasureId() != null) {
        UnitMeasure um = unitMeasureRepo.findById(request.getUnitMeasureId()).orElse(null);
        product.setUnitMeasure(um);
      }
      product.setSalePricePen(request.getSalePricePen());
      product.setEstimatedCostPen(request.getEstimatedCostPen());
      product.setBrand(request.getBrand());
      product.setModel(request.getModel());
      product.setShortDescription(request.getShortDescription());
      product.setTechnicalSpec(request.getTechnicalSpec());
      return mapper.toResponse(repo.save(product));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ProductResponse> cambiarEstado(Long id, Integer status) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      Product p = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
      p.setStatus(status);
      return mapper.toResponse(repo.save(p));
    })).subscribeOn(Schedulers.boundedElastic());
  }
}
