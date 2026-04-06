package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.dto.mapper.SaleMapper;
import org.autoservicio.backendcontratoservicio.dto.request.SaleInstallmentRequest;
import org.autoservicio.backendcontratoservicio.dto.request.SaleItemRequest;
import org.autoservicio.backendcontratoservicio.dto.request.SalePaymentRequest;
import org.autoservicio.backendcontratoservicio.dto.request.SaleRequest;
import org.autoservicio.backendcontratoservicio.dto.response.SaleResponse;
import org.autoservicio.backendcontratoservicio.entity.DocumentSeries;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.entity.SaleInstallment;
import org.autoservicio.backendcontratoservicio.entity.SaleItem;
import org.autoservicio.backendcontratoservicio.entity.SalePayment;
import org.autoservicio.backendcontratoservicio.entity.catalog.PaymentMethod;
import org.autoservicio.backendcontratoservicio.job.SunatDocumentJobService;
import org.autoservicio.backendcontratoservicio.jparepository.DocumentSeriesJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.ProductJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.ServiceEntityJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.PaymentMethodJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.UnitMeasureJpaRepo;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ClientesRepository;
import org.autoservicio.backendcontratoservicio.specification.SaleSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {
  private final SaleJpaRepo saleRepo;
  private final SaleMapper mapper;
  private final ProductJpaRepo productRepo;
  private final ServiceEntityJpaRepo serviceRepo;
  private final UnitMeasureJpaRepo unitMeasureRepo;
  private final PaymentMethodJpaRepo paymentMethodRepo;
  private final ClientesRepository clientesRepository;
  private final PlatformTransactionManager txManager;
  private final DocumentSeriesJpaRepo documentSeriesRepo;
  private final SaleDocumentJpaRepo saleDocumentRepo;
  private final SunatDocumentJobService sunatDocumentJobService;
  private final TicketPdfService ticketPdfService;

  // Executes a JPA block inside a real transaction on the calling thread (boundedElastic).
  // @Transactional on the method cannot be used here because subscribeOn() moves execution
  // to a different thread and Spring's ThreadLocal transaction does not propagate.
  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  public Mono<List<SaleResponse>> listar(Long clientId, String saleStatus,
                                         LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(() -> inReadTx(status -> {
      Specification<Sale> spec = SaleSpec.build(clientId, saleStatus, startDate, endDate);
      return saleRepo.findAll(spec).stream()
          .map(s -> enrichWithClient(mapper.toResponse(s))).toList();
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<SaleResponse> obtenerPorId(Long id) {
    return Mono.fromCallable(() -> inReadTx(status -> {
      Sale sale = saleRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
      return enrichWithClient(mapper.toResponse(sale));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<SaleResponse> crear(SaleRequest request) {
    return Mono.fromCallable(() -> {

      // PHASE 1: Create sale + SaleDocument inside a single transaction
      long[] ids = inTx(status -> {
        Sale sale = new Sale();
        sale.setClientId(request.getClientId());
        sale.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now());
        sale.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "CONTADO");
        sale.setPurchaseOrder(request.getPurchaseOrder());
        sale.setObservations(request.getObservations());
        sale.setSaleStatus("ACTIVO");
        sale.setCurrencyCode("PEN");

        BigDecimal taxPct = request.getTaxPercentage() != null
            ? request.getTaxPercentage() : new BigDecimal("18.00");
        sale.setTaxPercentage(taxPct);

        // Temporarily save to get ID for FK relationships
        Sale savedSale = saleRepo.save(sale);

        // Build items
        List<SaleItem> items = buildItems(request.getItems(), savedSale, taxPct);
        savedSale.getItems().addAll(items);

        // Calculate totals from items
        BigDecimal subtotal = items.stream().map(SaleItem::getSubtotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = items.stream().map(i ->
            i.getUnitPrice().multiply(i.getQuantity())
                .multiply(i.getDiscountPercentage())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = items.stream().map(SaleItem::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = items.stream().map(SaleItem::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedSale.setSubtotalAmount(subtotal);
        savedSale.setDiscountAmount(discount);
        savedSale.setTaxAmount(tax);
        savedSale.setTotalAmount(total);

        // Build payments
        if (request.getPayments() != null) {
          for (SalePaymentRequest payReq : request.getPayments()) {
            SalePayment payment = new SalePayment();
            payment.setSale(savedSale);
            PaymentMethod pm = paymentMethodRepo.findById(payReq.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException(
                    "Método de pago no encontrado: " + payReq.getPaymentMethodId()));
            payment.setPaymentMethod(pm);
            payment.setAmount(payReq.getAmount() != null ? payReq.getAmount() : BigDecimal.ZERO);
            payment.setChangeAmount(payReq.getChangeAmount() != null ? payReq.getChangeAmount() : BigDecimal.ZERO);
            payment.setPaymentDate(payReq.getPaymentDate() != null ? payReq.getPaymentDate() : LocalDate.now());
            payment.setReferenceNumber(payReq.getReferenceNumber());
            payment.setNotes(payReq.getNotes());
            savedSale.getPayments().add(payment);
          }
        }

        // Build installments for CREDITO
        if ("CREDITO".equals(savedSale.getPaymentType()) && request.getInstallments() != null) {
          for (SaleInstallmentRequest instReq : request.getInstallments()) {
            SaleInstallment inst = new SaleInstallment();
            inst.setSale(savedSale);
            inst.setAmount(instReq.getAmount() != null ? instReq.getAmount() : BigDecimal.ZERO);
            inst.setDueDate(instReq.getDueDate());
            inst.setStatus("PENDIENTE");
            savedSale.getInstallments().add(inst);
          }
        }

        Sale finalSale = saleRepo.save(savedSale);

        // Auto-detect document type based on client
        String docTypeCode = "03"; // default: Boleta
        if (request.getClientId() != null) {
          try {
            List<BuscarClientes> clients = clientesRepository.buscarclientes(
                String.valueOf(request.getClientId()));
            if (!clients.isEmpty() && clients.get(0).getTipodocident() != null
                && clients.get(0).getTipodocident() == 7) {
              docTypeCode = "01"; // Factura para RUC
            }
          } catch (Exception ignored) {}
        }

        // Find active document series
        List<DocumentSeries> seriesList = documentSeriesRepo
            .findByDocumentTypeSunatCodeAndStatusOrderBySeriesAsc(docTypeCode, 1);
        if (seriesList.isEmpty()) {
          log.warn("No hay serie activa para tipo de documento: {}, venta se guarda sin documento SUNAT", docTypeCode);
          return new long[]{finalSale.getId(), -1L};
        }

        DocumentSeries ds = documentSeriesRepo.findByIdWithLock(seriesList.get(0).getId())
            .orElseThrow();
        int nextSeq = ds.getCurrentSequence() + 1;
        ds.setCurrentSequence(nextSeq);
        documentSeriesRepo.save(ds);

        // Create SaleDocument
        SaleDocument document = new SaleDocument();
        document.setSale(finalSale);
        document.setDocumentTypeSunat(ds.getDocumentTypeSunat());
        document.setDocumentSeries(ds);
        document.setSeries(ds.getSeries());
        document.setSequence(nextSeq);
        document.setIssueDate(finalSale.getSaleDate());
        document.setStatus("PENDIENTE");
        saleDocumentRepo.save(document);

        return new long[]{finalSale.getId(), document.getId()};
      });

      long saleId = ids[0];
      long documentId = ids[1];

      // PHASE 2: Send to SUNAT synchronously (outside the main tx, uses its own @Transactional)
      if (documentId > 0) {
        sunatDocumentJobService.sendSaleDocumentNow(documentId);
      }

      // PHASE 3: Generate ticket PDF, upload to Google Drive, store URL in sale + sale_document
      if (documentId > 0) {
        String pdfUrl = ticketPdfService.uploadForSale(saleId, documentId);
        final String finalUrl = pdfUrl;
        final long finalDocId = documentId;
        final long finalSaleId = saleId;
        inTx(s -> {
          SaleDocument d = saleDocumentRepo.findById(finalDocId).orElseThrow();
          d.setPdfUrl(finalUrl);
          saleDocumentRepo.save(d);
          Sale sa = saleRepo.findById(finalSaleId).orElseThrow();
          sa.setTicketPdfUrl(finalUrl);
          saleRepo.save(sa);
          return null;
        });
      }

      // PHASE 4: Return enriched response
      return inReadTx(s -> {
        Sale sa = saleRepo.findById(saleId).orElseThrow();
        return enrichWithClient(mapper.toResponse(sa));
      });

    }).subscribeOn(Schedulers.boundedElastic());
  }

  private List<SaleItem> buildItems(List<SaleItemRequest> itemRequests, Sale sale, BigDecimal taxPct) {
    if (itemRequests == null) return new ArrayList<>();
    List<SaleItem> items = new ArrayList<>();
    for (SaleItemRequest req : itemRequests) {
      SaleItem item = new SaleItem();
      item.setSale(sale);
      item.setItemType(req.getItemType() != null ? req.getItemType() : "PRODUCT");
      item.setDescription(req.getDescription());
      item.setQuantity(req.getQuantity() != null ? req.getQuantity() : BigDecimal.ONE);
      item.setUnitPrice(req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO);
      item.setDiscountPercentage(req.getDiscountPercentage() != null ? req.getDiscountPercentage() : BigDecimal.ZERO);

      if (req.getProductId() != null) {
        productRepo.findById(req.getProductId()).ifPresent(p -> {
          item.setProduct(p);
          item.setSku(p.getSku());
          if (item.getDescription() == null) item.setDescription(p.getName());
        });
      }
      if (req.getServiceId() != null) {
        serviceRepo.findById(req.getServiceId()).ifPresent(s -> {
          item.setService(s);
          item.setSku(s.getSku());
          if (item.getDescription() == null) item.setDescription(s.getName());
        });
      }
      if (req.getUnitMeasureId() != null) {
        unitMeasureRepo.findById(req.getUnitMeasureId()).ifPresent(item::setUnitMeasure);
      }

      // unitPrice already includes IGV — extract base imponible and IGV from the final price
      BigDecimal gross = item.getUnitPrice().multiply(item.getQuantity());
      BigDecimal discountAmt = gross.multiply(item.getDiscountPercentage())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      BigDecimal totalWithIgv = gross.subtract(discountAmt);
      BigDecimal divisor = BigDecimal.ONE.add(
          taxPct.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
      BigDecimal net = totalWithIgv.divide(divisor, 2, RoundingMode.HALF_UP);
      BigDecimal taxAmt = totalWithIgv.subtract(net);

      item.setSubtotalAmount(net);
      item.setTaxAmount(taxAmt);
      item.setTotalAmount(totalWithIgv);
      items.add(item);
    }
    return items;
  }

  private SaleResponse enrichWithClient(SaleResponse response) {
    try {
      List<BuscarClientes> clients = clientesRepository.buscarclientes(
          String.valueOf(response.getClientId()));
      if (!clients.isEmpty()) {
        BuscarClientes c = clients.get(0);
        String fullName = c.getNombre_completo() != null ? c.getNombre_completo()
            : ((c.getNombres() != null ? c.getNombres() : "") + " "
            + (c.getApellidos() != null ? c.getApellidos() : "")).trim();
        response.setClientName(fullName);
        response.setClientDocNumber(c.getNrodocident());
      }
    } catch (Exception ignored) {
      // Client enrichment is non-critical
    }
    return response;
  }
}
