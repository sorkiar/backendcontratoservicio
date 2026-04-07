package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.CreditDebitNoteMapper;
import org.autoservicio.backendcontratoservicio.dto.mapper.RemissionGuideMapper;
import org.autoservicio.backendcontratoservicio.dto.mapper.SaleMapper;
import org.autoservicio.backendcontratoservicio.dto.response.ComprobanteResumenResponse;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteResponse;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideResponse;
import org.autoservicio.backendcontratoservicio.dto.response.SaleDocumentResponse;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.interfaces.IParamaeRepo;
import org.autoservicio.backendcontratoservicio.job.SunatDocumentJobService;
import org.autoservicio.backendcontratoservicio.jparepository.CreditDebitNoteJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.RemissionGuideJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleJpaRepo;
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
  private final SaleJpaRepo saleRepo;
  private final SaleMapper saleMapper;
  private final CreditDebitNoteMapper creditDebitNoteMapper;
  private final RemissionGuideMapper remissionGuideMapper;
  private final PlatformTransactionManager txManager;
  private final TicketPdfService ticketPdfService;
  private final PdfReportService pdfReportService;
  private final GoogleDriveService googleDriveService;
  private final IParamaeRepo paramaeRepo;
  private final SunatDocumentJobService jobService;

  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  // -------------------------------------------------------------------------
  // Listado unificado (ventas, notas, guías)
  // -------------------------------------------------------------------------

  public Mono<List<ComprobanteResumenResponse>> listarComprobantes(String status, String docTypeCode,
                                                                    LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      List<ComprobanteResumenResponse> result = new ArrayList<>();

      boolean incluirVentas = docTypeCode == null || "01".equals(docTypeCode) || "03".equals(docTypeCode);
      boolean incluirNotas  = docTypeCode == null || "07".equals(docTypeCode) || "08".equals(docTypeCode);
      boolean incluirGuias  = docTypeCode == null || "09".equals(docTypeCode);

      // Documentos de venta (Facturas / Boletas)
      if (incluirVentas) {
        String ventaCode = incluirVentas && docTypeCode != null ? docTypeCode : null;
        Specification<SaleDocument> spec = SaleDocumentSpec.build(status, ventaCode, startDate, endDate);
        saleDocumentRepo.findAll(spec).forEach(d -> result.add(toResumenVenta(d)));
      }

      // Notas de crédito / débito
      if (incluirNotas) {
        List<CreditDebitNote> notes = (status != null && !status.isBlank())
            ? creditDebitNoteRepo.findByStatusOrderByCreatedAtDesc(status)
            : creditDebitNoteRepo.findAll();
        notes.stream()
            .filter(n -> matchesDateRange(n.getIssueDate(), startDate, endDate))
            .filter(n -> docTypeCode == null
                || (n.getDocumentTypeSunat() != null && docTypeCode.equals(n.getDocumentTypeSunat().getCode())))
            .forEach(n -> result.add(toResumenNota(n)));
      }

      // Guías de remisión
      if (incluirGuias) {
        List<RemissionGuide> guides = (status != null && !status.isBlank())
            ? remissionGuideRepo.findByStatusOrderByCreatedAtDesc(status)
            : remissionGuideRepo.findAll();
        guides.stream()
            .filter(g -> matchesDateRange(g.getIssueDate(), startDate, endDate))
            .forEach(g -> result.add(toResumenGuia(g)));
      }

      result.sort(Comparator.comparing(ComprobanteResumenResponse::getFechaEmision,
          Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(Comparator.comparing(ComprobanteResumenResponse::getId).reversed()));

      return result;
    })).subscribeOn(Schedulers.boundedElastic());
  }

  private ComprobanteResumenResponse toResumenVenta(SaleDocument d) {
    ComprobanteResumenResponse r = new ComprobanteResumenResponse();
    r.setTipoEntidad("VENTA_DOC");
    r.setId(d.getId());
    if (d.getDocumentTypeSunat() != null) {
      r.setTipoDocumentoCodigo(d.getDocumentTypeSunat().getCode());
      r.setTipoDocumentoNombre(d.getDocumentTypeSunat().getName());
    }
    r.setSerie(d.getSeries());
    r.setCorrelativo(d.getSequence());
    r.setFechaEmision(d.getIssueDate());
    r.setStatus(d.getStatus());
    r.setSunatResponseCode(d.getSunatResponseCode());
    r.setSunatMessage(d.getSunatMessage());
    r.setPdfUrl(d.getPdfUrl());
    if (d.getSale() != null) r.setSaleId(d.getSale().getId());
    return r;
  }

  private ComprobanteResumenResponse toResumenNota(CreditDebitNote n) {
    ComprobanteResumenResponse r = new ComprobanteResumenResponse();
    r.setTipoEntidad("NOTA");
    r.setId(n.getId());
    if (n.getDocumentTypeSunat() != null) {
      r.setTipoDocumentoCodigo(n.getDocumentTypeSunat().getCode());
      r.setTipoDocumentoNombre(n.getDocumentTypeSunat().getName());
    }
    r.setSerie(n.getSeries());
    r.setCorrelativo(n.getSequence());
    r.setFechaEmision(n.getIssueDate());
    r.setStatus(n.getStatus());
    r.setSunatResponseCode(n.getSunatResponseCode());
    r.setSunatMessage(n.getSunatMessage());
    r.setPdfUrl(n.getPdfUrl());
    if (n.getSale() != null) r.setSaleId(n.getSale().getId());
    return r;
  }

  private ComprobanteResumenResponse toResumenGuia(RemissionGuide g) {
    ComprobanteResumenResponse r = new ComprobanteResumenResponse();
    r.setTipoEntidad("GUIA");
    r.setId(g.getId());
    r.setTipoDocumentoCodigo("09");
    r.setTipoDocumentoNombre("GUÍA DE REMISIÓN ELECTRÓNICA");
    r.setSerie(g.getSeries());
    r.setCorrelativo(g.getSequence());
    r.setFechaEmision(g.getIssueDate());
    r.setStatus(g.getStatus());
    r.setSunatResponseCode(g.getSunatResponseCode());
    r.setSunatMessage(g.getSunatMessage());
    r.setPdfUrl(g.getPdfUrl());
    r.setClientId(g.getClientId());
    return r;
  }

  private boolean matchesDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
    if (startDate != null && (date == null || date.isBefore(startDate))) return false;
    if (endDate != null && (date == null || date.isAfter(endDate))) return false;
    return true;
  }

  public Mono<SaleDocumentResponse> obtenerDocumento(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      SaleDocument doc = saleDocumentRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
      return saleMapper.toDocumentResponse(doc);
    })).subscribeOn(Schedulers.boundedElastic());
  }

  // -------------------------------------------------------------------------
  // Reenvío a SUNAT (valida que no esté ya EMITIDO)
  // -------------------------------------------------------------------------

  public Mono<SaleDocumentResponse> reenviarDocumento(Long id) {
    return Mono.fromCallable(() -> {
      inReadTx(txStatus -> {
        SaleDocument doc = saleDocumentRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
        if ("EMITIDO".equals(doc.getStatus())) {
          throw new RuntimeException(
              "El documento ya fue emitido a SUNAT (estado: EMITIDO). No se puede reenviar.");
        }
        return null;
      });
      jobService.sendSaleDocumentNow(id);
      return inReadTx(txStatus -> {
        SaleDocument doc = saleDocumentRepo.findByIdWithType(id).orElseThrow();
        return saleMapper.toDocumentResponse(doc);
      });
    }).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<CreditDebitNoteResponse> reenviarNota(Long id) {
    return Mono.fromCallable(() -> {
      inReadTx(txStatus -> {
        CreditDebitNote note = creditDebitNoteRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Nota no encontrada: " + id));
        if ("EMITIDO".equals(note.getStatus())) {
          throw new RuntimeException(
              "La nota ya fue emitida a SUNAT (estado: EMITIDO). No se puede reenviar.");
        }
        return null;
      });
      jobService.sendCreditDebitNoteNow(id);
      return inReadTx(txStatus ->
          creditDebitNoteMapper.toResponse(
              creditDebitNoteRepo.findByIdWithDetails(id).orElseThrow()));
    }).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<RemissionGuideResponse> reenviarGuia(Long id) {
    return Mono.fromCallable(() -> {
      inReadTx(txStatus -> {
        RemissionGuide guide = remissionGuideRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));
        if ("EMITIDO".equals(guide.getStatus())) {
          throw new RuntimeException(
              "La guía ya fue emitida a SUNAT (estado: EMITIDO). No se puede reenviar.");
        }
        return null;
      });
      jobService.sendRemissionGuideNow(id);
      return inReadTx(txStatus -> {
        RemissionGuide g = remissionGuideRepo.findByIdWithItems(id).orElseThrow();
        remissionGuideRepo.findByIdWithDrivers(id).ifPresent(gd -> g.setDrivers(gd.getDrivers()));
        return remissionGuideMapper.toResponse(g);
      });
    }).subscribeOn(Schedulers.boundedElastic());
  }

  // -------------------------------------------------------------------------
  // Regeneración de PDF
  // -------------------------------------------------------------------------

  /**
   * Regenera el ticket PDF de una venta y actualiza la URL en sale_document y sale.
   */
  public Mono<SaleDocumentResponse> regenerarPdfVenta(Long documentId) {
    return Mono.fromCallable(() -> {
      // Cargar saleId desde el documento (lazy load dentro de TX)
      long saleId = inReadTx(tx -> {
        SaleDocument doc = saleDocumentRepo.findByIdWithSale(documentId)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));
        return doc.getSale().getId();
      });

      // Regenerar y subir a Google Drive
      String pdfUrl = ticketPdfService.uploadForSale(saleId, documentId);

      // Persistir nueva URL en sale_document y sale
      return inTx(tx -> {
        SaleDocument doc = saleDocumentRepo.findByIdWithType(documentId).orElseThrow();
        doc.setPdfUrl(pdfUrl);
        saleDocumentRepo.save(doc);

        Sale sale = saleRepo.findById(saleId).orElseThrow();
        sale.setTicketPdfUrl(pdfUrl);
        saleRepo.save(sale);

        return saleMapper.toDocumentResponse(doc);
      });
    }).subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * Regenera el PDF A4 de una nota de crédito/débito y actualiza la URL.
   */
  public Mono<CreditDebitNoteResponse> regenerarPdfNota(Long noteId) {
    return pdfReportService.generarPdfNotaCreditoDebito(noteId)
        .flatMap(bytes -> Mono.fromCallable(() -> {
          String[] meta = inReadTx(tx -> {
            CreditDebitNote n = creditDebitNoteRepo.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada: " + noteId));
            return new String[]{n.getSeries(), String.format("%08d", n.getSequence())};
          });
          String pdfUrl = uploadToDrive(bytes, "nota-" + meta[0] + "-" + meta[1] + ".pdf");
          return inTx(tx -> {
            CreditDebitNote note = creditDebitNoteRepo.findById(noteId).orElseThrow();
            note.setPdfUrl(pdfUrl);
            return creditDebitNoteMapper.toResponse(creditDebitNoteRepo.save(note));
          });
        }).subscribeOn(Schedulers.boundedElastic()));
  }

  /**
   * Regenera el PDF A4 de una guía de remisión y actualiza la URL.
   */
  public Mono<RemissionGuideResponse> regenerarPdfGuia(Long guideId) {
    return pdfReportService.generarPdfGuiaRemision(guideId)
        .flatMap(bytes -> Mono.fromCallable(() -> {
          String[] meta = inReadTx(tx -> {
            RemissionGuide g = remissionGuideRepo.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + guideId));
            return new String[]{g.getSeries(), String.format("%08d", g.getSequence())};
          });
          String pdfUrl = uploadToDrive(bytes, "guia-" + meta[0] + "-" + meta[1] + ".pdf");
          return inTx(tx -> {
            RemissionGuide guide = remissionGuideRepo.findById(guideId).orElseThrow();
            guide.setPdfUrl(pdfUrl);
            return remissionGuideMapper.toResponse(remissionGuideRepo.save(guide));
          });
        }).subscribeOn(Schedulers.boundedElastic()));
  }

  // -------------------------------------------------------------------------
  // Helper: sube bytes PDF a Google Drive y retorna la URL pública
  // -------------------------------------------------------------------------

  private String uploadToDrive(byte[] bytes, String fileName) {
    File tempFile = null;
    try {
      tempFile = File.createTempFile("doc-", ".pdf");
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write(bytes);
      }
      String folderId = paramaeRepo.buscar_x_ID("DRV", "CONTRA").getValorstring();
      File namedFile = new File(tempFile.getParent(), fileName);
      if (tempFile.renameTo(namedFile)) {
        tempFile = namedFile;
      }
      return googleDriveService.uploadFile(tempFile, folderId);
    } catch (Exception e) {
      throw new RuntimeException("Error subiendo PDF a Google Drive: " + e.getMessage(), e);
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
  }
}
