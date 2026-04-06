package org.autoservicio.backendcontratoservicio.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNoteItem;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuideDriver;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuideItem;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.entity.SaleInstallment;
import org.autoservicio.backendcontratoservicio.entity.SaleItem;
import org.autoservicio.backendcontratoservicio.entity.SunatRequestLog;
import org.autoservicio.backendcontratoservicio.jparepository.CreditDebitNoteJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.RemissionGuideJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SunatRequestLogJpaRepo;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ClientesRepository;
import org.autoservicio.backendcontratoservicio.response.FacturacionResponse;
import org.autoservicio.backendcontratoservicio.util.ConfiguracionUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles NC/ND (07, 08) and Guías de Remisión (09) SUNAT submissions.
 * Facturas/Boletas (01, 03) continue to be handled by the existing DocumentoJobSerivce.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SunatDocumentJobService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${sunat.url}")
    private String facturadorUrl;

    @Value("${sunat.url-guia}")
    private String facturadorUrlGuia;

    @Value("${sunat.url-guia-valid}")
    private String facturadorUrlGuiaValid;

    private final CreditDebitNoteJpaRepo creditDebitNoteRepo;
    private final RemissionGuideJpaRepo remissionGuideRepo;
    private final SaleDocumentJpaRepo saleDocumentRepo;
    private final SaleJpaRepo saleJpaRepo;
    private final SunatRequestLogJpaRepo sunatRequestLogRepo;
    private final ConfiguracionUtil configuracionUtil;
    private final ClientesRepository clientesRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 60000)
    public void scheduledTick() {
        log.info("SunatDocumentJobService: iniciando ciclo...");
        try {
            processPendingSaleDocuments();
            processPendingCreditDebitNotes();
            processPendingRemissionGuides();
        } catch (Exception e) {
            log.error("Error en ciclo SunatDocumentJobService: {}", e.getMessage());
        }
        log.info("SunatDocumentJobService: ciclo completado.");
    }

    private void processPendingSaleDocuments() {
        List<SaleDocument> pending = saleDocumentRepo.findByStatusOrderByCreatedAtDesc("PENDIENTE");
        if (pending.isEmpty()) return;
        log.info("Procesando {} documentos de venta pendientes", pending.size());
        for (SaleDocument doc : pending) {
            try {
                sendSaleDocumentNow(doc.getId());
            } catch (Exception e) {
                log.error("Error procesando documento de venta {}: {}", doc.getId(), e.getMessage());
            }
        }
    }

    private void processPendingCreditDebitNotes() {
        List<CreditDebitNote> pending = creditDebitNoteRepo.findByStatusOrderByCreatedAtDesc("PENDIENTE");
        if (pending.isEmpty()) return;
        log.info("Procesando {} notas de crédito/débito pendientes", pending.size());
        for (CreditDebitNote note : pending) {
            try {
                sendCreditDebitNoteNow(note.getId());
            } catch (Exception e) {
                log.error("Error procesando nota {}: {}", note.getId(), e.getMessage());
            }
        }
    }

    private void processPendingRemissionGuides() {
        List<RemissionGuide> pending = remissionGuideRepo.findByStatusOrderByCreatedAtDesc("PENDIENTE");
        if (pending.isEmpty()) return;
        log.info("Procesando {} guías de remisión pendientes", pending.size());
        for (RemissionGuide guide : pending) {
            try {
                sendRemissionGuideNow(guide.getId());
            } catch (Exception e) {
                log.error("Error procesando guía {}: {}", guide.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void sendCreditDebitNoteNow(Long noteId) {
        CreditDebitNote note = creditDebitNoteRepo.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada: " + noteId));

        SunatRequestLog logEntry = new SunatRequestLog();
        logEntry.setDocumentType("credit_debit_note");
        logEntry.setDocumentId(noteId);

        try {
            Map<String, Object> payload = buildCreditDebitNotePayload(note);
            logEntry.setRequestPayload(payload.toString());

            log.info("Enviando nota {}/{}-{} a SUNAT",
                    note.getDocumentTypeSunat().getCode(), note.getSeries(), note.getSequence());

            note.setStatus("EN_PROCESO");
            creditDebitNoteRepo.save(note);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
                    facturadorUrl, HttpMethod.POST, new HttpEntity<>(payload, headers),
                    FacturacionResponse.class);

            FacturacionResponse factResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && factResponse != null && factResponse.isSuccess()) {
                FacturacionResponse.FacturacionData data = factResponse.getData();
                note.setStatus("ACEPTADO".equalsIgnoreCase(data.getTypeResultadoDeclaracion())
                        ? "ENVIADO" : "ERROR");
                note.setSunatMessage(data.getMessage());
                note.setSunatResponseCode(String.valueOf(data.getResponseCode()));
                note.setHashCode(data.getCodigoHash());
                note.setQrCode(data.getCadenaQr());
                note.setXmlBase64(data.getBase64Xml());
                note.setCdrBase64(data.getBase64XmlCdr());
                logEntry.setSuccess(true);
                logEntry.setHttpStatus(response.getStatusCode().value());
                logEntry.setResponsePayload(data.getMessage());
            } else {
                String errorMsg = response.getStatusCode().toString()
                        + (factResponse != null ? " - " + factResponse.getMessage() : "");
                note.setStatus("ERROR");
                note.setSunatMessage(errorMsg);
                logEntry.setSuccess(false);
                logEntry.setHttpStatus(response.getStatusCode().value());
                logEntry.setErrorMessage(errorMsg);
            }
            creditDebitNoteRepo.save(note);

        } catch (Exception e) {
            log.error("Error enviando nota {} a SUNAT: {}", noteId, e.getMessage());
            note.setStatus("ERROR");
            note.setSunatMessage(e.getMessage());
            creditDebitNoteRepo.save(note);
            logEntry.setSuccess(false);
            logEntry.setErrorMessage(e.getMessage());
        } finally {
            sunatRequestLogRepo.save(logEntry);
        }
    }

    @Transactional
    public void sendRemissionGuideNow(Long guideId) {
        RemissionGuide guide = remissionGuideRepo.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + guideId));

        SunatRequestLog logEntry = new SunatRequestLog();
        logEntry.setDocumentType("remission_guide");
        logEntry.setDocumentId(guideId);

        try {
            Map<String, Object> payload = buildRemissionGuidePayload(guide);
            logEntry.setRequestPayload(payload.toString());

            log.info("Enviando guía 09/{}-{} a SUNAT", guide.getSeries(), guide.getSequence());

            guide.setStatus("EN_PROCESO");
            remissionGuideRepo.save(guide);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
                    facturadorUrlGuia, HttpMethod.POST, new HttpEntity<>(payload, headers),
                    FacturacionResponse.class);

            FacturacionResponse factResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && factResponse != null && factResponse.isSuccess()) {
                FacturacionResponse.FacturacionData data = factResponse.getData();
                guide.setStatus("ACEPTADO".equalsIgnoreCase(data.getTypeResultadoDeclaracion())
                        ? "ENVIADO" : "ERROR");
                guide.setSunatMessage(data.getMessage());
                guide.setSunatResponseCode(String.valueOf(data.getResponseCode()));
                guide.setHashCode(data.getCodigoHash());
                guide.setQrCode(data.getCadenaQr());
                guide.setXmlBase64(data.getBase64Xml());
                guide.setCdrBase64(data.getBase64XmlCdr());
                logEntry.setSuccess(true);
                logEntry.setHttpStatus(response.getStatusCode().value());
                logEntry.setResponsePayload(data.getMessage());
            } else {
                String errorMsg = response.getStatusCode().toString()
                        + (factResponse != null ? " - " + factResponse.getMessage() : "");
                guide.setStatus("ERROR");
                guide.setSunatMessage(errorMsg);
                logEntry.setSuccess(false);
                logEntry.setHttpStatus(response.getStatusCode().value());
                logEntry.setErrorMessage(errorMsg);
            }
            remissionGuideRepo.save(guide);

        } catch (Exception e) {
            log.error("Error enviando guía {} a SUNAT: {}", guideId, e.getMessage());
            guide.setStatus("ERROR");
            guide.setSunatMessage(e.getMessage());
            remissionGuideRepo.save(guide);
            logEntry.setSuccess(false);
            logEntry.setErrorMessage(e.getMessage());
        } finally {
            sunatRequestLogRepo.save(logEntry);
        }
    }

    // ─── SaleDocument send ────────────────────────────────────────────────────

    @Transactional
    public void sendSaleDocumentNow(Long documentId) {
        SaleDocument doc = saleDocumentRepo.findByIdWithType(documentId)
                .orElseThrow(() -> new RuntimeException("SaleDocument no encontrado: " + documentId));

        // Hibernate proxy: can get FK id without loading the entity
        Long saleId = doc.getSale().getId();
        Sale sale = saleJpaRepo.findByIdWithItems(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + saleId));

        SunatRequestLog logEntry = new SunatRequestLog();
        logEntry.setDocumentType("sale_document");
        logEntry.setDocumentId(documentId);

        try {
            Map<String, Object> payload = buildSaleDocumentPayload(doc, sale);
            logEntry.setRequestPayload(payload.toString());

            log.info("Enviando documento de venta {}/{}-{} a SUNAT",
                    doc.getDocumentTypeSunat().getCode(), doc.getSeries(), doc.getSequence());

            doc.setStatus("EN_PROCESO");
            saleDocumentRepo.save(doc);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
                    facturadorUrl, HttpMethod.POST, new HttpEntity<>(payload, headers),
                    FacturacionResponse.class);

            FacturacionResponse factResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && factResponse != null && factResponse.isSuccess()) {
                FacturacionResponse.FacturacionData data = factResponse.getData();
                doc.setStatus("ACEPTADO".equalsIgnoreCase(data.getTypeResultadoDeclaracion())
                        ? "ENVIADO" : "ERROR");
                doc.setSunatMessage(data.getMessage());
                doc.setSunatResponseCode(String.valueOf(data.getResponseCode()));
                doc.setHashCode(data.getCodigoHash());
                doc.setQrCode(data.getCadenaQr());
                doc.setXmlBase64(data.getBase64Xml());
                doc.setCdrBase64(data.getBase64XmlCdr());
                logEntry.setSuccess(true);
                logEntry.setHttpStatus(response.getStatusCode().value());
                logEntry.setResponsePayload(data.getMessage());
                saleDocumentRepo.save(doc);
            } else {
                String errorMsg = response.getStatusCode().toString()
                        + (factResponse != null ? " - " + factResponse.getMessage() : "");
                doc.setStatus("ERROR");
                doc.setSunatMessage(errorMsg);
                logEntry.setSuccess(false);
                logEntry.setHttpStatus(response.getStatusCode().value());
                logEntry.setErrorMessage(errorMsg);
                saleDocumentRepo.save(doc);
                throw new RuntimeException("Error SUNAT: " + errorMsg);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error enviando documento de venta {} a SUNAT: {}", documentId, e.getMessage());
            doc.setStatus("ERROR");
            doc.setSunatMessage(e.getMessage());
            saleDocumentRepo.save(doc);
            logEntry.setSuccess(false);
            logEntry.setErrorMessage(e.getMessage());
            throw new RuntimeException("Error enviando a SUNAT: " + e.getMessage(), e);
        } finally {
            sunatRequestLogRepo.save(logEntry);
        }
    }

    private Map<String, Object> buildSaleDocumentPayload(SaleDocument doc, Sale sale) {
        Map<String, String> cfg = configuracionUtil.obtenerGrupo("empresa_emisora");
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("empresa", buildEmpresaBlock(cfg));

        // Cliente
        String[] clienteData = resolveCliente(sale.getClientId(), "venta " + sale.getId());
        String clieNumDoc = clienteData[0];
        String clieRazonSocial = clienteData[1];
        String tipoDocIdent = clienteData[2];

        String docTypeCode = doc.getDocumentTypeSunat().getCode();
        String tipoDocumento = "01".equals(docTypeCode) ? "FACTURA" : "BOLETA";
        boolean esCredito = "CREDITO".equals(sale.getPaymentType());

        Map<String, Object> comprobante = new LinkedHashMap<>();
        comprobante.put("tipoDocumento", tipoDocumento);
        comprobante.put("compSerie", doc.getSeries());
        comprobante.put("compNumero", doc.getSequence());
        comprobante.put("compPorcentajeIgv", sale.getTaxPercentage() != null
                ? sale.getTaxPercentage() : new BigDecimal("18"));
        comprobante.put("compCondicionPago", esCredito ? "CREDITO" : "CONTADO");
        comprobante.put("compFechaEmision", doc.getIssueDate() != null
                ? doc.getIssueDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT));
        comprobante.put("moneda", sale.getCurrencyCode() != null ? sale.getCurrencyCode() : "PEN");
        if (sale.getObservations() != null) comprobante.put("compObservaciones", sale.getObservations());
        if (sale.getPurchaseOrder() != null) comprobante.put("compOrdenCompra", sale.getPurchaseOrder());

        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("clieNumeroDocumento", clieNumDoc);
        cliente.put("clieRazonSocial", clieRazonSocial);
        cliente.put("tipoDocumentoIdentidad", tipoDocIdent);
        comprobante.put("cliente", cliente);

        comprobante.put("lsItemComprobante", buildSaleItems(sale));

        // Cuotas (solo para CREDITO)
        if (esCredito && sale.getInstallments() != null && !sale.getInstallments().isEmpty()) {
            List<Map<String, Object>> cuotas = new ArrayList<>();
            int num = 1;
            for (SaleInstallment inst : sale.getInstallments()) {
                Map<String, Object> cuota = new LinkedHashMap<>();
                cuota.put("itcuCuota", num++);
                cuota.put("itcuFecha", inst.getDueDate() != null
                        ? inst.getDueDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT));
                cuota.put("itcuMonto", inst.getAmount());
                cuotas.add(cuota);
            }
            comprobante.put("lsItemCuota", cuotas);
        }

        json.put("comprobante", comprobante);
        return json;
    }

    private List<Map<String, Object>> buildSaleItems(Sale sale) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (sale.getItems() == null) return items;
        for (SaleItem si : sale.getItems()) {
            BigDecimal precioUnitario = si.getUnitPrice() != null ? si.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal cantidad = si.getQuantity() != null ? si.getQuantity() : BigDecimal.ONE;
            BigDecimal subtotal = si.getSubtotalAmount() != null ? si.getSubtotalAmount() : BigDecimal.ZERO;
            BigDecimal igv = si.getTaxAmount() != null ? si.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal total = si.getTotalAmount() != null ? si.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal valorUnitario = cantidad.compareTo(BigDecimal.ZERO) > 0
                    ? subtotal.divide(cantidad, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itcoUnidadMedida", si.getUnitMeasure() != null
                    ? si.getUnitMeasure().getCodeSunat() : "NIU");
            item.put("itcoDescripcion", si.getDescription() != null ? si.getDescription() : "");
            item.put("itcoCantidad", cantidad);
            item.put("itcoValorUnitario", valorUnitario.setScale(2, RoundingMode.HALF_UP));
            item.put("itcoPrecioUnitario", precioUnitario.setScale(2, RoundingMode.HALF_UP));
            item.put("itcoDescuentoAfecta", BigDecimal.ZERO);
            item.put("itcoSubTotal", subtotal.setScale(2, RoundingMode.HALF_UP));
            item.put("itcoIgv", igv.setScale(2, RoundingMode.HALF_UP));
            item.put("itcoIcbper", BigDecimal.ZERO);
            item.put("itcoDescuentoNoAfecta", BigDecimal.ZERO);
            item.put("itcoTotal", total.setScale(2, RoundingMode.HALF_UP));
            item.put("tipoAfectacionIgv", "GRAVADO");
            items.add(item);
        }
        return items;
    }

    // ─── Payload builders ────────────────────────────────────────────────────

    private Map<String, Object> buildCreditDebitNotePayload(CreditDebitNote note) {
        Map<String, String> cfg = configuracionUtil.obtenerGrupo("empresa_emisora");
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("empresa", buildEmpresaBlock(cfg));

        // Cliente
        String[] clienteData = resolveCliente(note.getSale().getClientId(), "nota " + note.getId());
        String clieNumDoc = clienteData[0];
        String clieRazonSocial = clienteData[1];
        String tipoDocIdent = clienteData[2];

        String docTypeCode = note.getDocumentTypeSunat().getCode();
        String tipoDocumento = "07".equals(docTypeCode) ? "NOTA_CREDITO" : "NOTA_DEBITO";

        // Objeto nota anidado (estructura IDMH)
        String origDocCode = note.getOriginalDocument().getDocumentTypeSunat().getCode();
        Map<String, Object> notaObj = new LinkedHashMap<>();
        notaObj.put("notaSerieModifica", note.getOriginalDocument().getSeries());
        notaObj.put("notaNumeroModifica", note.getOriginalDocument().getSequence());
        notaObj.put("notaFechaModifica", note.getOriginalDocument().getIssueDate() != null
                ? note.getOriginalDocument().getIssueDate().format(DATE_FMT)
                : LocalDate.now().format(DATE_FMT));
        notaObj.put("tipoNotaCreditoDebito", mapNoteTypeCode(note.getCreditDebitNoteType().getCode()));
        notaObj.put("tipoDocumentoAfecta", "01".equals(origDocCode) ? "FACTURA" : "BOLETA");

        Map<String, Object> comprobante = new LinkedHashMap<>();
        comprobante.put("tipoDocumento", tipoDocumento);
        comprobante.put("compSerie", note.getSeries());
        comprobante.put("compNumero", note.getSequence());
        comprobante.put("compPorcentajeIgv", note.getTaxPercentage() != null
                ? note.getTaxPercentage() : new BigDecimal("18"));
        comprobante.put("compCondicionPago", "CONTADO");
        comprobante.put("compFechaEmision", note.getIssueDate() != null
                ? note.getIssueDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT));
        comprobante.put("moneda", note.getCurrencyCode() != null ? note.getCurrencyCode() : "PEN");
        if (note.getReason() != null) comprobante.put("compObservaciones", note.getReason());

        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("clieNumeroDocumento", clieNumDoc);
        cliente.put("clieRazonSocial", clieRazonSocial);
        cliente.put("tipoDocumentoIdentidad", tipoDocIdent);
        comprobante.put("cliente", cliente);

        comprobante.put("lsItemComprobante", buildNoteItems(note.getItems()));
        comprobante.put("nota", notaObj);
        json.put("comprobante", comprobante);
        return json;
    }

    private Map<String, Object> buildRemissionGuidePayload(RemissionGuide guide) {
        Map<String, String> cfg = configuracionUtil.obtenerGrupo("empresa_emisora");
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("empresa", buildEmpresaBlockForGuia(cfg));

        // Destinatario → cliente del comprobante
        String[] clienteData = resolveCliente(guide.getClientId(), "guía " + guide.getId());
        String destNumDoc = clienteData[0];
        String destRazonSocial = clienteData[1];
        String destTipoDoc = clienteData[2];

        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("clieNumeroDocumento", destNumDoc);
        cliente.put("clieRazonSocial", destRazonSocial);
        cliente.put("tipoDocumentoIdentidad", destTipoDoc);

        // Items en formato ItemComprobante (valores monetarios en cero para guía)
        List<Map<String, Object>> items = new ArrayList<>();
        if (guide.getItems() != null) {
            for (RemissionGuideItem it : guide.getItems()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("itcoUnidadMedida", it.getUnitMeasureSunat() != null ? it.getUnitMeasureSunat() : "NIU");
                item.put("itcoDescripcion", it.getDescription() != null ? it.getDescription() : "");
                item.put("itcoCantidad", it.getQuantity() != null ? it.getQuantity() : BigDecimal.ONE);
                item.put("itcoValorUnitario", BigDecimal.ZERO);
                item.put("itcoPrecioUnitario", BigDecimal.ZERO);
                item.put("itcoDescuentoAfecta", BigDecimal.ZERO);
                item.put("itcoSubTotal", BigDecimal.ZERO);
                item.put("itcoIgv", BigDecimal.ZERO);
                item.put("itcoIcbper", BigDecimal.ZERO);
                item.put("itcoDescuentoNoAfecta", BigDecimal.ZERO);
                item.put("itcoTotal", BigDecimal.ZERO);
                item.put("tipoAfectacionIgv", "GRAVADO");
                items.add(item);
            }
        }
        // La guía necesita al menos un ítem
        if (items.isEmpty()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itcoUnidadMedida", "NIU");
            item.put("itcoDescripcion", "Traslado de bienes");
            item.put("itcoCantidad", BigDecimal.ONE);
            item.put("itcoValorUnitario", BigDecimal.ZERO);
            item.put("itcoPrecioUnitario", BigDecimal.ZERO);
            item.put("itcoDescuentoAfecta", BigDecimal.ZERO);
            item.put("itcoSubTotal", BigDecimal.ZERO);
            item.put("itcoIgv", BigDecimal.ZERO);
            item.put("itcoIcbper", BigDecimal.ZERO);
            item.put("itcoDescuentoNoAfecta", BigDecimal.ZERO);
            item.put("itcoTotal", BigDecimal.ZERO);
            item.put("tipoAfectacionIgv", "GRAVADO");
            items.add(item);
        }

        // Objeto guia anidado
        String tipoTransporte = mapTransportMode(guide.getTransportMode());
        Map<String, Object> guia = new LinkedHashMap<>();
        guia.put("guiaPesoBruto", guide.getGrossWeight() != null ? guide.getGrossWeight() : BigDecimal.ZERO);
        guia.put("guiaNumeroBultos", guide.getPackageCount() != null ? guide.getPackageCount() : 1);
        guia.put("guiaFechaTraslado", guide.getTransferDate() != null
                ? guide.getTransferDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT));
        guia.put("guiaPuntoPartidaDireccion", guide.getOriginAddress() != null ? guide.getOriginAddress() : "");
        guia.put("guiaUbigeoPartida", guide.getOriginUbigeo() != null ? guide.getOriginUbigeo() : "");
        guia.put("guiaPuntoLlegadaDireccion", guide.getDestinationAddress() != null ? guide.getDestinationAddress() : "");
        guia.put("guiaUbigeoLlegada", guide.getDestinationUbigeo() != null ? guide.getDestinationUbigeo() : "");
        if (guide.getWeightUnit() != null) guia.put("guiaUnidadMedidaPeso", guide.getWeightUnit());
        guia.put("guiaTrasladoVehiculoMenores", Boolean.TRUE.equals(guide.getMinorVehicleTransfer()));
        if (guide.getTransferReasonDescription() != null)
            guia.put("guiaMotivoTrasladoDescripcion", guide.getTransferReasonDescription());
        guia.put("motivoTraslado", mapMotivoTraslado(guide.getTransferReason()));
        guia.put("tipoTransporte", tipoTransporte);

        if ("TRANSPORTE_PUBLICO".equals(tipoTransporte)) {
            Map<String, Object> transportista = new LinkedHashMap<>();
            transportista.put("tipoDocumentoIdentidad", "RUC");
            transportista.put("clieNumeroDocumento", guide.getCarrierRuc() != null ? guide.getCarrierRuc() : "");
            transportista.put("clieRazonSocial", guide.getCarrierName() != null ? guide.getCarrierName() : "");
            if (guide.getCarrierAuthorizationCode() != null)
                transportista.put("guiaRegistroMtcTransportista", guide.getCarrierAuthorizationCode());
            guia.put("transportista", transportista);
        } else {
            List<Map<String, Object>> lsGuiaTransporte = new ArrayList<>();
            if (guide.getDrivers() != null) {
                for (RemissionGuideDriver d : guide.getDrivers()) {
                    Map<String, Object> conductor = new LinkedHashMap<>();
                    conductor.put("gutrConductorTipoDocumento", d.getDriverDocType() != null ? d.getDriverDocType() : "1");
                    conductor.put("gutrConductorNumeroDocumento", d.getDriverDocNumber() != null ? d.getDriverDocNumber() : "");
                    conductor.put("gutrConductorNombres", d.getDriverFirstName() != null ? d.getDriverFirstName() : "");
                    conductor.put("gutrConductorApellidos", d.getDriverLastName() != null ? d.getDriverLastName() : "");
                    conductor.put("gutrConductorNumeroLicencia", d.getDriverLicenseNumber() != null ? d.getDriverLicenseNumber() : "");
                    if (d.getVehiclePlate() != null && !d.getVehiclePlate().isBlank())
                        conductor.put("gutrVehiculoPlaca", d.getVehiclePlate());
                    lsGuiaTransporte.add(conductor);
                }
            }
            guia.put("lsGuiaTransporte", lsGuiaTransporte);
        }

        // Comprobante envolvente
        Map<String, Object> comprobante = new LinkedHashMap<>();
        comprobante.put("tipoDocumento", "GUIA_REMISION_REMITENTE");
        comprobante.put("compSerie", guide.getSeries());
        comprobante.put("compNumero", guide.getSequence());
        comprobante.put("compPorcentajeIgv", new BigDecimal("18"));
        comprobante.put("compCondicionPago", "CONTADO");
        comprobante.put("compFechaEmision", guide.getIssueDate() != null
                ? guide.getIssueDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT));
        comprobante.put("moneda", "PEN");
        comprobante.put("cliente", cliente);
        comprobante.put("lsItemComprobante", items);
        comprobante.put("guia", guia);

        json.put("comprobante", comprobante);
        return json;
    }

    // ─── Client lookup helper ─────────────────────────────────────────────────

    /**
     * Looks up a client by ID and returns [nroDoc, razonSocial, tipoDocIdent].
     * Throws RuntimeException if the client is not found or has blank critical fields.
     */
    private String[] resolveCliente(Long clientId, String contexto) {
        if (clientId == null) {
            throw new RuntimeException("ClientId es nulo para " + contexto);
        }
        BuscarClientes c;
        try {
            c = clientesRepository.buscarClientePorId(clientId.intValue());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar cliente " + clientId + " para " + contexto + ": " + e.getMessage(), e);
        }
        if (c == null) {
            throw new RuntimeException("Cliente no encontrado con ID " + clientId + " para " + contexto);
        }
        String nroDoc = c.getNrodocident() != null ? c.getNrodocident() : "";
        String razonSocial = c.getNombre_completo() != null ? c.getNombre_completo()
                : ((c.getNombres() != null ? c.getNombres() : "") + " "
                + (c.getApellidos() != null ? c.getApellidos() : "")).trim();
        if (nroDoc.isBlank() || razonSocial.isBlank()) {
            throw new RuntimeException("Datos incompletos del cliente " + clientId
                    + " para " + contexto + ": nroDoc='" + nroDoc + "', razonSocial='" + razonSocial + "'");
        }
        String tipoDoc = (c.getTipodocident() != null && c.getTipodocident() == 7) ? "RUC" : "DNI";
        return new String[]{nroDoc, razonSocial, tipoDoc};
    }

    // ─── Catalog mapping helpers ──────────────────────────────────────────────

    private static final Map<String, String> NOTE_TYPE_CODE_MAP = Map.ofEntries(
            Map.entry("C01", "CREDITO_ANULACION_OPERACION"),
            Map.entry("C02", "CREDITO_ANULACION_ERROR_RUC"),
            Map.entry("C03", "CREDITO_CORRECCION_ERROR_DESCRIPCION"),
            Map.entry("C04", "CREDITO_DESCUENTO_GLOBAL"),
            Map.entry("C05", "CREDITO_DESCUENTO_POR_ITEM"),
            Map.entry("C06", "CREDITO_DEVOLUCION_TOTAL"),
            Map.entry("C07", "CREDITO_DEVOLUCION_POR_ITEM"),
            Map.entry("C08", "CREDITO_BONIFICACION"),
            Map.entry("C09", "CREDITO_DISMINUCION_VALOR"),
            Map.entry("C10", "CREDITO_OTROS_CONCEPTOS"),
            Map.entry("C11", "CREDITO_AJUSTES_OPERACIONES_EXPORTACION"),
            Map.entry("C12", "CREDITO_AJUSTES_IVAP"),
            Map.entry("C13", "CREDITO_AJUSTES_FECHA_MONTO_CREDITO"),
            Map.entry("D01", "DEBITO_INTERESES_MORA"),
            Map.entry("D02", "DEBITO_AUMENTO_VALOR"),
            Map.entry("D03", "DEBITO_PENALIDADES"),
            Map.entry("D11", "DEBITO_AJUSTES_OPERACIONES_EXPORTACION"),
            Map.entry("D12", "DEBITO_AJUSTES_IVAP")
    );

    private static final Map<String, String> MOTIVO_TRASLADO_MAP = Map.ofEntries(
            Map.entry("01", "VENTA"),
            Map.entry("02", "COMPRA"),
            Map.entry("04", "TRASLADO_EMPRESA"),
            Map.entry("08", "IMPORTACION"),
            Map.entry("09", "EXPORTACION"),
            Map.entry("13", "OTROS"),
            Map.entry("14", "VENTA_CONFIRMACION"),
            Map.entry("18", "TRASLADO_ITINERANTE"),
            Map.entry("19", "TRASLADO_ZONA_PRIMARIA")
    );

    private String mapNoteTypeCode(String code) {
        return NOTE_TYPE_CODE_MAP.getOrDefault(code, "CREDITO_ANULACION_OPERACION");
    }

    /** Accepts catalog code ("01"…) or enum name ("VENTA"…). */
    private String mapMotivoTraslado(String value) {
        if (value == null) return "VENTA";
        if (MOTIVO_TRASLADO_MAP.containsKey(value)) return MOTIVO_TRASLADO_MAP.get(value);
        return value; // already an enum name
    }

    /** Accepts catalog code ("01" / "02") or enum name. */
    private String mapTransportMode(String value) {
        if (value == null) return "TRANSPORTE_PRIVADO";
        if ("01".equals(value)) return "TRANSPORTE_PUBLICO";
        if ("02".equals(value)) return "TRANSPORTE_PRIVADO";
        return value;
    }

    /** Empresa block for guías — adds emprGuiaId + emprGuiaClave. */
    private Map<String, Object> buildEmpresaBlockForGuia(Map<String, String> cfg) {
        Map<String, Object> empresa = buildEmpresaBlock(cfg);
        empresa.put("emprGuiaId", cfg.get("emprGuiaId"));
        empresa.put("emprGuiaClave", cfg.get("emprGuiaClave"));
        return empresa;
    }

    private Map<String, Object> buildEmpresaBlock(Map<String, String> cfg) {
        Map<String, Object> empresa = new LinkedHashMap<>();
        empresa.put("emprRuc", cfg.get("emprRuc"));
        empresa.put("emprRazonSocial", cfg.get("emprRazonSocial"));
        empresa.put("emprDireccionFiscal", cfg.get("emprDireccionFiscal"));
        empresa.put("emprCodigoEstablecimientoSunat", cfg.get("emprCodigoEstablecimientoSunat"));
        empresa.put("ubigeo", Map.of(
                "ubigUbigeo", cfg.getOrDefault("ubigUbigeo", ""),
                "ubigDepartamento", cfg.getOrDefault("ubigDepartamento", ""),
                "ubigProvincia", cfg.getOrDefault("ubigProvincia", ""),
                "ubigDistrito", cfg.getOrDefault("ubigDistrito", "")
        ));
        empresa.put("emprLeyAmazonia", Boolean.parseBoolean(cfg.getOrDefault("emprLeyAmazonia", "false")));
        empresa.put("emprProduccion", Boolean.parseBoolean(cfg.getOrDefault("emprProduccion", "false")));
        empresa.put("emprCertificadoLLavePublica", cfg.get("emprCertificadoLlavePublica"));
        empresa.put("emprCertificadoLLavePrivada", cfg.get("emprCertificadoLlavePrivada"));
        empresa.put("emprUsuarioSecundario", cfg.get("emprUsuarioSecundario"));
        empresa.put("emprClaveUsuarioSecundario", cfg.get("emprClaveUsuarioSecundario"));
        return empresa;
    }

    private List<Map<String, Object>> buildNoteItems(List<CreditDebitNoteItem> noteItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (noteItems == null) return items;
        for (CreditDebitNoteItem ni : noteItems) {
            BigDecimal precioUnitario = ni.getUnitPrice() != null ? ni.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal cantidad = ni.getQuantity() != null ? ni.getQuantity() : BigDecimal.ONE;
            BigDecimal valorUnitario = precioUnitario
                    .divide(BigDecimal.valueOf(1.18), 6, RoundingMode.HALF_UP);
            BigDecimal subtotal = valorUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
            BigDecimal igv = subtotal.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(igv);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itcoUnidadMedida", ni.getUnitMeasure() != null
                    ? ni.getUnitMeasure().getCodeSunat() : "NIU");
            item.put("itcoDescripcion", ni.getDescription() != null ? ni.getDescription() : "");
            item.put("itcoCantidad", cantidad);
            item.put("itcoValorUnitario", valorUnitario.setScale(2, RoundingMode.HALF_UP));
            item.put("itcoPrecioUnitario", precioUnitario.setScale(2, RoundingMode.HALF_UP));
            item.put("itcoDescuentoAfecta", BigDecimal.ZERO);
            item.put("itcoSubTotal", subtotal);
            item.put("itcoIgv", igv);
            item.put("itcoIcbper", BigDecimal.ZERO);
            item.put("itcoDescuentoNoAfecta", BigDecimal.ZERO);
            item.put("itcoTotal", total);
            item.put("tipoAfectacionIgv", "GRAVADO");
            items.add(item);
        }
        return items;
    }
}
