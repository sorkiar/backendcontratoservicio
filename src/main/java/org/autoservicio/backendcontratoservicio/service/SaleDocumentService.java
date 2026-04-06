package org.autoservicio.backendcontratoservicio.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.entity.DocumentSeries;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;
import org.autoservicio.backendcontratoservicio.jparepository.DocumentSeriesJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.catalog.DocumentTypeSunatJpaRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaleDocumentService {
  private final SaleDocumentJpaRepo saleDocumentRepo;
  private final DocumentSeriesJpaRepo documentSeriesRepo;
  private final DocumentTypeSunatJpaRepo documentTypeSunatRepo;

  @Transactional
  public SaleDocument generateDocument(Sale sale, String clientDocType, Long documentSeriesId) {
    // "6" = RUC → Factura (01), otherwise Boleta (03)
    String docTypeCode = "6".equals(clientDocType) ? "01" : "03";

    DocumentTypeSunat docType = documentTypeSunatRepo.findById(docTypeCode)
        .orElseThrow(() -> new RuntimeException("Tipo comprobante no encontrado: " + docTypeCode));

    DocumentSeries series = null;
    int sequence = 1;
    String seriesCode = null;

    if (documentSeriesId != null) {
      series = documentSeriesRepo.findByIdWithLock(documentSeriesId)
          .orElseThrow(() -> new RuntimeException("Serie no encontrada: " + documentSeriesId));
      sequence = series.getCurrentSequence() + 1;
      series.setCurrentSequence(sequence);
      documentSeriesRepo.save(series);
      seriesCode = series.getSeries();
    }

    SaleDocument doc = new SaleDocument();
    doc.setSale(sale);
    doc.setDocumentTypeSunat(docType);
    doc.setDocumentSeries(series);
    doc.setSeries(seriesCode);
    doc.setSequence(sequence);
    doc.setIssueDate(LocalDate.now());
    doc.setStatus("PENDIENTE");

    return saleDocumentRepo.save(doc);
  }
}
