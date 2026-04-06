package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.CreditDebitNoteMapper;
import org.autoservicio.backendcontratoservicio.dto.mapper.RemissionGuideMapper;
import org.autoservicio.backendcontratoservicio.dto.mapper.SaleMapper;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteResponse;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideResponse;
import org.autoservicio.backendcontratoservicio.dto.response.SaleDocumentResponse;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.jparepository.CreditDebitNoteJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.RemissionGuideJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.specification.SaleDocumentSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class DocumentResendService {
  private final SaleDocumentJpaRepo saleDocumentRepo;
  private final CreditDebitNoteJpaRepo creditDebitNoteRepo;
  private final RemissionGuideJpaRepo remissionGuideRepo;
  private final SaleMapper saleMapper;
  private final CreditDebitNoteMapper creditDebitNoteMapper;
  private final RemissionGuideMapper remissionGuideMapper;
  private final PlatformTransactionManager txManager;

  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  public Mono<List<SaleDocumentResponse>> listarComprobantes(String status, String docTypeCode,
                                                             LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      Specification<SaleDocument> spec = SaleDocumentSpec.build(status, docTypeCode, startDate, endDate);
      return saleDocumentRepo.findAll(spec).stream().map(saleMapper::toDocumentResponse).toList();
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<SaleDocumentResponse> reenviarDocumento(Long id) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      SaleDocument doc = saleDocumentRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
      doc.setStatus("PENDIENTE");
      return saleMapper.toDocumentResponse(saleDocumentRepo.save(doc));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<CreditDebitNoteResponse> reenviarNota(Long id) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      CreditDebitNote note = creditDebitNoteRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Nota no encontrada: " + id));
      note.setStatus("PENDIENTE");
      return creditDebitNoteMapper.toResponse(creditDebitNoteRepo.save(note));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<RemissionGuideResponse> reenviarGuia(Long id) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      RemissionGuide guide = remissionGuideRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));
      guide.setStatus("PENDIENTE");
      return remissionGuideMapper.toResponse(remissionGuideRepo.save(guide));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<SaleDocumentResponse> obtenerDocumento(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      SaleDocument doc = saleDocumentRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
      return saleMapper.toDocumentResponse(doc);
    })).subscribeOn(Schedulers.boundedElastic());
  }
}
