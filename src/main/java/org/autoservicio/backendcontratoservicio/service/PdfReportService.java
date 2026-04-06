package org.autoservicio.backendcontratoservicio.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.autoservicio.backendcontratoservicio.entity.*;
import org.autoservicio.backendcontratoservicio.excepciones.RepositorioException;
import org.autoservicio.backendcontratoservicio.jparepository.*;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ClientesRepository;
import org.autoservicio.backendcontratoservicio.util.ConfiguracionUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final SaleJpaRepo saleJpaRepo;
    private final SaleDocumentJpaRepo saleDocumentJpaRepo;
    private final CreditDebitNoteJpaRepo creditDebitNoteJpaRepo;
    private final RemissionGuideJpaRepo remissionGuideJpaRepo;
    private final ClientesRepository clientesRepository;
    private final ConfiguracionUtil configuracionUtil;

    // -------------------------------------------------------------------------
    // Boleta / Factura
    // -------------------------------------------------------------------------

    public Mono<byte[]> generarPdfVenta(Long saleId, Long documentId) {
        return Mono.fromCallable(() -> {
            Sale sale = saleJpaRepo.findByIdWithItems(saleId)
                    .orElseThrow(() -> new RepositorioException("Venta no encontrada: " + saleId));
            SaleDocument doc = saleDocumentJpaRepo.findByIdWithType(documentId)
                    .orElseThrow(() -> new RepositorioException("Documento no encontrado: " + documentId));
            BuscarClientes client = buscarCliente(sale.getClientId());
            Map<String, String> cfg = configuracionUtil.obtenerGrupo("empresa_emisora");

            List<Map<String, Object>> dataList = buildVentaDataList(sale, doc, client, cfg);
            String jrxml = "01".equals(doc.getDocumentTypeSunat().getCode())
                    ? "/jasper/rpteFacturaElectronicaA4FormatoCompleto.jrxml"
                    : "/jasper/rpteBoletaElectronicaA4FormatoCompleto.jrxml";
            return fillAndExport(jrxml, dataList);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // -------------------------------------------------------------------------
    // Nota de Crédito / Débito
    // -------------------------------------------------------------------------

    public Mono<byte[]> generarPdfNotaCreditoDebito(Long noteId) {
        return Mono.fromCallable(() -> {
            CreditDebitNote note = creditDebitNoteJpaRepo.findByIdWithDetails(noteId)
                    .orElseThrow(() -> new RepositorioException("Nota no encontrada: " + noteId));
            Long clientId = note.getSale().getClientId();
            BuscarClientes client = buscarCliente(clientId);
            Map<String, String> cfg = configuracionUtil.obtenerGrupo("empresa_emisora");

            List<Map<String, Object>> dataList = buildNotaDataList(note, client, cfg);
            return fillAndExport("/jasper/NotaCreditoDebitoVentaA4.jrxml", dataList);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // -------------------------------------------------------------------------
    // Guía de Remisión
    // -------------------------------------------------------------------------

    public Mono<byte[]> generarPdfGuiaRemision(Long guideId) {
        return Mono.fromCallable(() -> {
            RemissionGuide guide = remissionGuideJpaRepo.findByIdWithItems(guideId)
                    .orElseThrow(() -> new RepositorioException("Guía no encontrada: " + guideId));
            RemissionGuide guideWithDrivers = remissionGuideJpaRepo.findByIdWithDrivers(guideId)
                    .orElseThrow(() -> new RepositorioException("Guía no encontrada: " + guideId));
            Map<String, String> cfg = configuracionUtil.obtenerGrupo("empresa_emisora");

            List<Map<String, Object>> dataList = buildGuiaDataList(guide, guideWithDrivers.getDrivers(), cfg);
            return fillAndExport("/jasper/GuiaRemisionVentaA4.jrxml", dataList);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // -------------------------------------------------------------------------
    // Builders — Venta (Boleta / Factura)
    // -------------------------------------------------------------------------

    private List<Map<String, Object>> buildVentaDataList(Sale sale, SaleDocument doc,
                                                          BuscarClientes client, Map<String, String> cfg) {
        List<SaleItem> items = sale.getItems();
        if (items == null || items.isEmpty()) {
            return Collections.singletonList(buildVentaRow(sale, doc, client, cfg, new SaleItem()));
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (SaleItem item : items) {
            dataList.add(buildVentaRow(sale, doc, client, cfg, item));
        }
        return dataList;
    }

    private Map<String, Object> buildVentaRow(Sale sale, SaleDocument doc,
                                               BuscarClientes client, Map<String, String> cfg, SaleItem item) {
        Map<String, Object> row = new HashMap<>();
        putEmpresaFields(row, cfg);
        // Cabecera
        row.put("tico_descripcion", doc.getDocumentTypeSunat().getName());
        row.put("comp_numero_comprobante",
                doc.getSeries() + "-" + String.format("%08d", doc.getSequence()));
        row.put("comp_descripcion_cliente", nvl(clientNombre(client)));
        row.put("comp_direccion_cliente", nvl(client.getDireccion()));
        row.put("clie_numero_documento", nvl(client.getNrodocident()));
        row.put("comp_descuento_global", 0);
        row.put("comp_fecha_emicion",
                doc.getIssueDate() != null ? Timestamp.valueOf(doc.getIssueDate().atStartOfDay()) : null);
        row.put("comp_descripcion_moneda", monedaDesc(sale.getCurrencyCode()));
        row.put("comp_simbolo_moneda", monedaSimbolo(sale.getCurrencyCode()));
        row.put("comp_codigo_hash", nvl(doc.getHashCode()));
        row.put("comp_cadena_qr", nvl(doc.getQrCode()));
        row.put("comp_vendedor", "");
        row.put("comp_condicion_pago", nvl(sale.getPaymentType()));
        row.put("comp_estado", nvl(doc.getStatus()));
        row.put("otros_tributos", 0.00);
        row.put("comp_id", sale.getId());
        row.put("observaciones", nvl(sale.getObservations()));
        row.put("numero_placa", "");
        row.put("marc_descripcion", "");
        row.put("priorizar_despacho", Boolean.FALSE);
        row.put("exonerado_igv", "N");
        // Ítem
        putSaleItemFields(row, item);
        return row;
    }

    // -------------------------------------------------------------------------
    // Builders — Nota de Crédito / Débito
    // -------------------------------------------------------------------------

    private List<Map<String, Object>> buildNotaDataList(CreditDebitNote note,
                                                         BuscarClientes client, Map<String, String> cfg) {
        SaleDocument origDoc = note.getOriginalDocument();
        String docRefNum = origDoc.getSeries() + "-" + String.format("%08d", origDoc.getSequence());
        String docRefTipo = origDoc.getDocumentTypeSunat().getName();
        String tipoNota = note.getCreditDebitNoteType().getName();

        List<CreditDebitNoteItem> items = note.getItems();
        if (items == null || items.isEmpty()) {
            return Collections.singletonList(
                    buildNotaRow(note, client, cfg, new CreditDebitNoteItem(), docRefNum, docRefTipo, tipoNota));
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (CreditDebitNoteItem item : items) {
            dataList.add(buildNotaRow(note, client, cfg, item, docRefNum, docRefTipo, tipoNota));
        }
        return dataList;
    }

    private Map<String, Object> buildNotaRow(CreditDebitNote note, BuscarClientes client,
                                              Map<String, String> cfg, CreditDebitNoteItem item,
                                              String docRefNum, String docRefTipo, String tipoNota) {
        Map<String, Object> row = new HashMap<>();
        putEmpresaFields(row, cfg);
        // Cabecera
        row.put("tico_descripcion", note.getDocumentTypeSunat().getName());
        row.put("comp_numero_comprobante",
                note.getSeries() + "-" + String.format("%08d", note.getSequence()));
        row.put("comp_descripcion_cliente", nvl(clientNombre(client)));
        row.put("comp_direccion_cliente", nvl(client.getDireccion()));
        row.put("clie_numero_documento", nvl(client.getNrodocident()));
        row.put("comp_descuento_global", 0);
        row.put("comp_fecha_emicion",
                note.getIssueDate() != null ? Timestamp.valueOf(note.getIssueDate().atStartOfDay()) : null);
        row.put("comp_descripcion_moneda", monedaDesc(note.getCurrencyCode()));
        row.put("comp_simbolo_moneda", monedaSimbolo(note.getCurrencyCode()));
        row.put("comp_codigo_hash", nvl(note.getHashCode()));
        row.put("comp_cadena_qr", nvl(note.getQrCode()));
        row.put("comp_vendedor", "");
        row.put("comp_condicion_pago", "");
        row.put("comp_estado", nvl(note.getStatus()));
        row.put("otros_tributos", 0.00);
        row.put("comp_id", note.getId());
        row.put("observaciones", nvl(note.getReason()));
        row.put("numero_placa", "");
        row.put("marc_descripcion", "");
        row.put("priorizar_despacho", Boolean.FALSE);
        row.put("exonerado_igv", "N");
        // Campos extra NC/ND
        row.put("doc_referencia", docRefNum);
        row.put("doc_referencia_tipo", docRefTipo);
        row.put("tipo_nota_desc", tipoNota);
        // Ítem
        BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal discPct = item.getDiscountPercentage() != null ? item.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal discount = discPct.multiply(unitPrice).multiply(qty)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        row.put("itco_codigo_interno", "");
        row.put("itco_cantidad", qty.doubleValue());
        row.put("itco_unidad_medida", item.getUnitMeasure() != null ? item.getUnitMeasure().getSymbol() : "NIU");
        row.put("itco_descripcion_completa", nvl(item.getDescription()));
        row.put("itco_precio_unitario", unitPrice.doubleValue());
        row.put("itco_descuento", discount);
        row.put("itco_tipo_igv", 1);
        row.put("itco_igv", item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO);
        row.put("icbper", 0.00);
        return row;
    }

    // -------------------------------------------------------------------------
    // Builders — Guía de Remisión
    // -------------------------------------------------------------------------

    private List<Map<String, Object>> buildGuiaDataList(RemissionGuide guide,
                                                         List<RemissionGuideDriver> drivers,
                                                         Map<String, String> cfg) {
        RemissionGuideDriver driver = (drivers != null && !drivers.isEmpty()) ? drivers.get(0) : null;
        List<RemissionGuideItem> items = guide.getItems();
        if (items == null || items.isEmpty()) {
            return Collections.singletonList(buildGuiaRow(guide, driver, cfg, new RemissionGuideItem()));
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (RemissionGuideItem item : items) {
            dataList.add(buildGuiaRow(guide, driver, cfg, item));
        }
        return dataList;
    }

    private Map<String, Object> buildGuiaRow(RemissionGuide guide, RemissionGuideDriver driver,
                                              Map<String, String> cfg, RemissionGuideItem item) {
        Map<String, Object> row = new HashMap<>();
        // Empresa
        row.put("empr_ruc", cfg.getOrDefault("emprRuc", ""));
        row.put("empr_razon_social", cfg.getOrDefault("emprRazonSocial", ""));
        row.put("empr_nombre_comercial",
                cfg.getOrDefault("emprNombreComercial", cfg.getOrDefault("emprRazonSocial", "")));
        row.put("empr_direccion_fiscal", cfg.getOrDefault("emprDireccionFiscal", ""));
        row.put("empr_telefono", cfg.getOrDefault("emprTelefono", ""));
        row.put("empr_pagina_web", cfg.getOrDefault("emprPaginaWeb", ""));
        row.put("empr_pdf_texto_inferior", cfg.getOrDefault("emprPdfTextoInferior", ""));
        row.put("empr_pdf_eslogan", cfg.getOrDefault("emprPdfEslogan", ""));
        row.put("empr_pdf_marca_agua", null);
        row.put("empr_numero_autorizacion", "");
        row.put("empr_imagen", null);
        row.put("empr_direccion_sucursal", null);
        // Cabecera guía
        row.put("tico_descripcion", "GUÍA DE REMISIÓN ELECTRÓNICA");
        row.put("comp_numero_comprobante",
                guide.getSeries() + "-" + String.format("%08d", guide.getSequence()));
        row.put("fecha_hora",
                guide.getIssueDate() != null ? Timestamp.valueOf(guide.getIssueDate().atStartOfDay()) : null);
        row.put("comp_fecha_traslado",
                guide.getTransferDate() != null ? java.sql.Date.valueOf(guide.getTransferDate()) : null);
        row.put("tipo_guia", nvl(guide.getTransferReason()));
        row.put("tipo_guia_descripcion", nvl(guide.getTransferReasonDescription()));
        row.put("direccionpartida", nvl(guide.getOriginAddress()));
        row.put("direccionllegada", nvl(guide.getDestinationAddress()));
        row.put("num_bultos", guide.getPackageCount() != null ? guide.getPackageCount() : 0);
        row.put("observaciones", nvl(guide.getObservations()));
        row.put("comp_cadena_qr", nvl(guide.getQrCode()));
        // Destinatario (cliente)
        BuscarClientes recipient = buscarClienteOpt(guide.getClientId());
        row.put("razonsocial", recipient != null ? nvl(clientNombre(recipient)) : "");
        row.put("ruccliente", recipient != null ? nvl(recipient.getNrodocident()) : "");
        row.put("dni_cliente", recipient != null ? nvl(recipient.getNrodocident()) : "");
        // Transportista
        row.put("transporte", nvl(guide.getCarrierName()));
        row.put("ructransporte", nvl(guide.getCarrierRuc()));
        // Conductor
        if (driver != null) {
            String nombre = (nvl(driver.getDriverFirstName()) + " " + nvl(driver.getDriverLastName())).trim();
            row.put("chofer", nombre);
            row.put("licencia", nvl(driver.getDriverLicenseNumber()));
            row.put("placa", nvl(driver.getVehiclePlate()));
        } else {
            row.put("chofer", "");
            row.put("licencia", "");
            row.put("placa", "");
        }
        // Ítem
        row.put("descripcion", nvl(item.getDescription()));
        row.put("cantidad", item.getQuantity() != null ? item.getQuantity().doubleValue() : 0.0);
        row.put("abreviatura", nvl(item.getUnitMeasureSunat(), "NIU"));
        row.put("codigo_producto",
                item.getProduct() != null ? nvl(item.getProduct().getSku()) : "");
        return row;
    }

    // -------------------------------------------------------------------------
    // Campos empresa compartidos
    // -------------------------------------------------------------------------

    private void putEmpresaFields(Map<String, Object> row, Map<String, String> cfg) {
        row.put("empr_ruc", cfg.getOrDefault("emprRuc", ""));
        row.put("empr_nombre_comercial",
                cfg.getOrDefault("emprNombreComercial", cfg.getOrDefault("emprRazonSocial", "")));
        row.put("empr_razon_social", cfg.getOrDefault("emprRazonSocial", ""));
        row.put("empr_direccion_fiscal", cfg.getOrDefault("emprDireccionFiscal", ""));
        row.put("empr_telefono", cfg.getOrDefault("emprTelefono", ""));
        row.put("empr_pagina_web", cfg.getOrDefault("emprPaginaWeb", ""));
        row.put("empr_numero_autorizacion", "");
        row.put("empr_imagen", null);
        row.put("empr_pdf_marca_agua", null);
        row.put("empr_pdf_texto_inferior", cfg.getOrDefault("emprPdfTextoInferior", ""));
        row.put("empr_pdf_eslogan", cfg.getOrDefault("emprPdfEslogan", ""));
        row.put("empr_direccion_sucursal", null);
    }

    private void putSaleItemFields(Map<String, Object> row, SaleItem item) {
        BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal discPct = item.getDiscountPercentage() != null ? item.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal discount = discPct.multiply(unitPrice).multiply(qty)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        row.put("itco_codigo_interno", nvl(item.getSku()));
        row.put("itco_cantidad", qty.doubleValue());
        row.put("itco_unidad_medida", item.getUnitMeasure() != null ? item.getUnitMeasure().getSymbol() : "NIU");
        row.put("itco_descripcion_completa", nvl(item.getDescription()));
        row.put("itco_precio_unitario", unitPrice.doubleValue());
        row.put("itco_descuento", discount);
        row.put("itco_tipo_igv", 1);
        row.put("itco_igv", item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO);
        row.put("icbper", 0.00);
    }

    // -------------------------------------------------------------------------
    // JasperReports core
    // -------------------------------------------------------------------------

    private byte[] fillAndExport(String jrxmlPath, List<Map<String, Object>> dataList) throws JRException {
        InputStream is = getClass().getResourceAsStream(jrxmlPath);
        if (is == null) {
            throw new RepositorioException("Plantilla no encontrada: " + jrxmlPath);
        }
        URL urlImagen = getClass().getResource("/static/images/logo-bc.png");
        Map<String, Object> params = new HashMap<>();
        params.put("urlImagen", urlImagen != null ? urlImagen.toString() : "");

        JasperReport report = JasperCompileManager.compileReport(is);
        JasperPrint print = JasperFillManager.fillReport(report, params,
                new JRBeanCollectionDataSource(dataList));
        return JasperExportManager.exportReportToPdf(print);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String nvl(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    private static String monedaDesc(String code) {
        return "PEN".equals(code) ? "SOLES" : "DÓLARES";
    }

    private static String monedaSimbolo(String code) {
        return "PEN".equals(code) ? "S/" : "$";
    }

    private BuscarClientes buscarCliente(Long clientId) {
        List<BuscarClientes> list = clientesRepository.buscarclientes(String.valueOf(clientId));
        if (list == null || list.isEmpty()) {
            throw new RepositorioException("Cliente no encontrado: " + clientId);
        }
        return list.get(0);
    }

    private BuscarClientes buscarClienteOpt(Long clientId) {
        if (clientId == null) return null;
        try {
            List<BuscarClientes> list = clientesRepository.buscarclientes(String.valueOf(clientId));
            return (list != null && !list.isEmpty()) ? list.get(0) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String clientNombre(BuscarClientes c) {
        if (c == null) return "";
        return c.getNombre_completo() != null ? c.getNombre_completo()
            : ((c.getNombres() != null ? c.getNombres() : "") + " "
               + (c.getApellidos() != null ? c.getApellidos() : "")).trim();
    }
}
