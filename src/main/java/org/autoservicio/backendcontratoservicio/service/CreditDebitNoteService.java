package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.CreditDebitNoteMapper;
import org.autoservicio.backendcontratoservicio.dto.request.CreditDebitNoteItemRequest;
import org.autoservicio.backendcontratoservicio.dto.request.CreditDebitNoteRequest;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteResponse;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNoteItem;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.entity.catalog.CreditDebitNoteType;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;
import org.autoservicio.backendcontratoservicio.jparepository.CreditDebitNoteJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.DocumentSeriesJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.ProductJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.ServiceEntityJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.CreditDebitNoteTypeJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.DocumentTypeSunatJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.UnitMeasureJpaRepo;
import org.autoservicio.backendcontratoservicio.specification.CreditDebitNoteSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class CreditDebitNoteService {
  private final CreditDebitNoteJpaRepo repo;
  private final CreditDebitNoteMapper mapper;
  private final SaleJpaRepo saleRepo;
  private final SaleDocumentJpaRepo saleDocumentRepo;
  private final DocumentSeriesJpaRepo documentSeriesRepo;
  private final DocumentTypeSunatJpaRepo documentTypeSunatRepo;
  private final CreditDebitNoteTypeJpaRepo noteTypeRepo;
  private final ProductJpaRepo productRepo;
  private final ServiceEntityJpaRepo serviceRepo;
  private final UnitMeasureJpaRepo unitMeasureRepo;
  private final PlatformTransactionManager txManager;

  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  public Mono<List<CreditDebitNoteResponse>> listar(Long saleId, String status,
                                                    LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      Specification<CreditDebitNote> spec = CreditDebitNoteSpec.build(saleId, status, startDate, endDate);
      return repo.findAll(spec).stream().map(mapper::toResponse).toList();
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<CreditDebitNoteResponse> obtenerPorId(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      CreditDebitNote note = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Nota no encontrada: " + id));
      return mapper.toResponse(note);
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<CreditDebitNoteResponse> crear(CreditDebitNoteRequest request) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      Sale sale = saleRepo.findById(request.getSaleId())
          .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + request.getSaleId()));
      SaleDocument origDoc = saleDocumentRepo.findById(request.getOriginalDocumentId())
          .orElseThrow(() -> new RuntimeException("Documento original no encontrado: " + request.getOriginalDocumentId()));
      DocumentTypeSunat docType = documentTypeSunatRepo.findById(request.getDocumentTypeCode())
          .orElseThrow(() -> new RuntimeException("Tipo comprobante no encontrado: " + request.getDocumentTypeCode()));
      CreditDebitNoteType noteType = noteTypeRepo.findById(request.getCreditDebitNoteTypeCode())
          .orElseThrow(() -> new RuntimeException("Tipo nota no encontrado: " + request.getCreditDebitNoteTypeCode()));

      CreditDebitNote note = new CreditDebitNote();
      note.setSale(sale);
      note.setOriginalDocument(origDoc);
      note.setDocumentTypeSunat(docType);
      note.setCreditDebitNoteType(noteType);
      note.setReason(request.getReason());
      note.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
      note.setStatus("PENDIENTE");
      note.setCurrencyCode("PEN");
      note.setTaxPercentage(new BigDecimal("18.00"));

      if (request.getDocumentSeriesId() != null) {
        documentSeriesRepo.findByIdWithLock(request.getDocumentSeriesId()).ifPresent(ds -> {
          int next = ds.getCurrentSequence() + 1;
          ds.setCurrentSequence(next);
          documentSeriesRepo.save(ds);
          note.setDocumentSeries(ds);
          note.setSeries(ds.getSeries());
          note.setSequence(next);
        });
      }

      List<CreditDebitNoteItem> items = buildItems(request.getItems(), note);
      note.setItems(items);

      BigDecimal subtotal = items.stream().map(CreditDebitNoteItem::getSubtotalAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal tax = items.stream().map(CreditDebitNoteItem::getTaxAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      note.setSubtotalAmount(subtotal);
      note.setTaxAmount(tax);
      note.setTotalAmount(subtotal.add(tax));

      return mapper.toResponse(repo.save(note));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  private List<CreditDebitNoteItem> buildItems(List<CreditDebitNoteItemRequest> itemRequests,
                                               CreditDebitNote note) {
    if (itemRequests == null) return new ArrayList<>();
    List<CreditDebitNoteItem> items = new ArrayList<>();
    for (CreditDebitNoteItemRequest req : itemRequests) {
      CreditDebitNoteItem item = new CreditDebitNoteItem();
      item.setCreditDebitNote(note);
      item.setItemType(req.getItemType() != null ? req.getItemType() : "PRODUCT");
      item.setDescription(req.getDescription());
      item.setQuantity(req.getQuantity() != null ? req.getQuantity() : BigDecimal.ONE);
      item.setUnitPrice(req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO);
      item.setDiscountPercentage(req.getDiscountPercentage() != null ? req.getDiscountPercentage() : BigDecimal.ZERO);
      if (req.getProductId() != null)
        productRepo.findById(req.getProductId()).ifPresent(item::setProduct);
      if (req.getServiceId() != null)
        serviceRepo.findById(req.getServiceId()).ifPresent(item::setService);
      if (req.getUnitMeasureId() != null)
        unitMeasureRepo.findById(req.getUnitMeasureId()).ifPresent(item::setUnitMeasure);
      // unitPrice already includes IGV — extract base imponible and IGV from the final price
      BigDecimal gross = item.getUnitPrice().multiply(item.getQuantity());
      BigDecimal discAmt = gross.multiply(item.getDiscountPercentage())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      BigDecimal totalWithIgv = gross.subtract(discAmt);
      BigDecimal net = totalWithIgv.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
      BigDecimal taxAmt = totalWithIgv.subtract(net);
      item.setSubtotalAmount(net);
      item.setTaxAmount(taxAmt);
      item.setTotalAmount(totalWithIgv);
      items.add(item);
    }
    return items;
  }
}
