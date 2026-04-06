package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.DocumentSeriesMapper;
import org.autoservicio.backendcontratoservicio.dto.response.DocumentSeriesResponse;
import org.autoservicio.backendcontratoservicio.entity.DocumentSeries;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;
import org.autoservicio.backendcontratoservicio.jparepository.DocumentSeriesJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.DocumentTypeSunatJpaRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class DocumentSeriesService {
  private final DocumentSeriesJpaRepo repo;
  private final DocumentSeriesMapper mapper;
  private final DocumentTypeSunatJpaRepo docTypeSunatRepo;
  private final PlatformTransactionManager txManager;

  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  public Mono<List<DocumentSeriesResponse>> listar() {
    return Mono.fromCallable(() -> inReadTx(txStatus ->
        repo.findByStatusOrderBySeriesAsc(1).stream().map(mapper::toResponse).toList()
    )).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<List<DocumentSeriesResponse>> listarPorTipo(String code) {
    return Mono.fromCallable(() -> inReadTx(txStatus ->
        repo.findByDocumentTypeSunatCodeAndStatusOrderBySeriesAsc(code, 1).stream().map(mapper::toResponse).toList()
    )).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<DocumentSeriesResponse> obtenerPorId(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      DocumentSeries ds = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Serie no encontrada: " + id));
      return mapper.toResponse(ds);
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<DocumentSeriesResponse> crear(String docTypeCode, String series) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      DocumentTypeSunat docType = docTypeSunatRepo.findById(docTypeCode)
          .orElseThrow(() -> new RuntimeException("Tipo de comprobante no encontrado: " + docTypeCode));
      DocumentSeries ds = new DocumentSeries();
      ds.setDocumentTypeSunat(docType);
      ds.setSeries(series);
      ds.setCurrentSequence(0);
      ds.setStatus(1);
      return mapper.toResponse(repo.save(ds));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<DocumentSeriesResponse> cambiarEstado(Long id, Integer status) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      DocumentSeries ds = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Serie no encontrada: " + id));
      ds.setStatus(status);
      return mapper.toResponse(repo.save(ds));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional
  public int nextSequence(Long seriesId) {
    DocumentSeries ds = repo.findByIdWithLock(seriesId)
        .orElseThrow(() -> new RuntimeException("Serie no encontrada: " + seriesId));
    int next = ds.getCurrentSequence() + 1;
    ds.setCurrentSequence(next);
    repo.save(ds);
    return next;
  }
}
