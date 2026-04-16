package org.autoservicio.backendcontratoservicio.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.autoservicio.backendcontratoservicio.interfaces.IParamaeRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleDocumentJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.SaleJpaRepo;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ClientesRepository;
import org.autoservicio.backendcontratoservicio.util.ConfiguracionUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPdfService {

    private final TemplateEngine templateEngine;
    private final ConfiguracionUtil configuracionUtil;
    private final PlatformTransactionManager txManager;
    private final SaleJpaRepo saleJpaRepo;
    private final SaleDocumentJpaRepo saleDocumentRepo;
    private final ClientesRepository clientesRepository;
    private final GoogleDriveService googleDriveService;
    private final IParamaeRepo paramaeRepo;

    private <T> T inReadTx(TransactionCallback<T> cb) {
        TransactionTemplate t = new TransactionTemplate(txManager);
        t.setReadOnly(true);
        return t.execute(cb);
    }

    /**
     * Loads sale + document + client data, initializing all lazy collections inside
     * a read-only transaction. Returns [Sale, SaleDocument, BuscarClientes (may be null)].
     */
    private Object[] loadData(Long saleId, Long documentId) {
        Object[] txData = inReadTx(status -> {
            Sale sa = saleJpaRepo.findByIdWithItems(saleId)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + saleId));
            // Force-initialize lazy collections while session is open
            sa.getPayments().size();
            sa.getInstallments().size();
            sa.getPayments().forEach(p -> {
                if (p.getPaymentMethod() != null) p.getPaymentMethod().getName();
            });
            SaleDocument doc = saleDocumentRepo.findByIdWithType(documentId)
                    .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));
            return new Object[]{sa, doc};
        });

        Sale sale = (Sale) txData[0];
        SaleDocument doc = (SaleDocument) txData[1];

        BuscarClientes client = null;
        try {
            client = clientesRepository.buscarClientePorId(sale.getClientId().intValue());
        } catch (Exception ignored) {}

        return new Object[]{sale, doc, client};
    }

    /**
     * Generates the ticket PDF bytes for on-demand download.
     */
    public byte[] generateForSale(Long saleId, Long documentId) {
        Object[] data = loadData(saleId, documentId);
        return generate((Sale) data[0], (SaleDocument) data[1], (BuscarClientes) data[2]);
    }

    /**
     * Generates the ticket PDF, uploads it to Google Drive (same folder as cobranza),
     * and returns the webViewLink URL to be stored in SaleDocument.pdfUrl.
     * Used by the admin regeneration endpoint.
     */
    public String uploadForSale(Long saleId, Long documentId) {
        Object[] data = loadData(saleId, documentId);
        SaleDocument doc = (SaleDocument) data[1];
        byte[] pdfBytes = generate((Sale) data[0], doc, (BuscarClientes) data[2]);
        return uploadBytesToDrive(pdfBytes, doc.getSeries(), doc.getSequence());
    }

    /**
     * Uploads pre-generated PDF bytes to Google Drive.
     * Used during sale creation so the bytes (already generated inside the DB transaction)
     * are not regenerated from DB again.
     */
    public String uploadBytesToDrive(byte[] pdfBytes, String series, Integer sequence) {
        String seriesStr = series != null ? series : "NV01";
        String seq = sequence != null ? String.format("%08d", sequence) : "00000001";
        String fileName = "ticket-" + seriesStr + "-" + seq + ".pdf";

        File tempFile = null;
        try {
            tempFile = File.createTempFile("ticket-", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }

            String folderId = paramaeRepo.buscar_x_ID("DRV", "CONTRA").getValorstring();
            File namedFile = new File(tempFile.getParent(), fileName);
            if (tempFile.renameTo(namedFile)) {
                tempFile = namedFile;
            }
            return googleDriveService.uploadFile(tempFile, folderId);
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo ticket PDF a Google Drive: " + e.getMessage(), e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Generates a ticket PDF for the given already-loaded (non-lazy) entities.
     * All lazy collections (payments, installments, paymentMethods) must already
     * be initialized before calling this method.
     */
    public byte[] generate(Sale sale, SaleDocument doc, BuscarClientes client) {
        try {
            EmpresaTicket empresa = buildEmpresa();
            VentaTicket venta = buildVenta(sale, doc, client);

            Context ctx = new Context(Locale.getDefault());
            ctx.setVariable("empresa", empresa);
            ctx.setVariable("venta", venta);
            ctx.setVariable("facturadorUrl", "magistrack-bc.website");

            String html = templateEngine.process("ticket", ctx);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando ticket PDF: " + e.getMessage(), e);
        }
    }

    private EmpresaTicket buildEmpresa() {
        Map<String, String> emisora = configuracionUtil.obtenerGrupo("empresa_emisora");
        Map<String, String> pdfCfg = configuracionUtil.obtenerGrupo("empresa_pdf");

        EmpresaTicket e = new EmpresaTicket();
        e.setRazonSocial(emisora.getOrDefault("emprRazonSocial", ""));
        e.setNombreComercial(pdfCfg.get("emprNombreComercial"));
        e.setDireccion(emisora.get("emprDireccionFiscal"));
        e.setDocumento(emisora.getOrDefault("emprRuc", ""));
        e.setTelefono(pdfCfg.get("emprTelefono"));
        e.setPaginaWeb(pdfCfg.get("emprPaginaWeb"));
        e.setEslogan(pdfCfg.get("emprPdfEslogan"));
        e.setLogoBase64(buildLogoBase64());
        return e;
    }

    private VentaTicket buildVenta(Sale sale, SaleDocument doc, BuscarClientes client) {
        VentaTicket v = new VentaTicket();

        // Tipo documento
        if (doc != null && doc.getDocumentTypeSunat() != null) {
            TipoDocVentaTicket tipo = new TipoDocVentaTicket();
            tipo.setNombre(doc.getDocumentTypeSunat().getName());
            v.setTipoDocumentoVenta(tipo);
        }

        // Serie y correlativo
        v.setSerie(doc != null && doc.getSeries() != null ? doc.getSeries() : "NV01");
        v.setCorrelativo(doc != null && doc.getSequence() != null
                ? String.format("%08d", doc.getSequence()) : "00000001");

        // Fecha
        v.setFecha(sale.getSaleDate());

        // Cliente
        if (client != null) {
            ClienteTicket c = new ClienteTicket();
            String nombre = client.getNombre_completo() != null ? client.getNombre_completo()
                    : ((client.getNombres() != null ? client.getNombres() : "") + " "
                    + (client.getApellidos() != null ? client.getApellidos() : "")).trim();
            c.setNombreCompleto(nombre);
            c.setRazonSocial(null);
            c.setNroDocumento(client.getNrodocident());
            if (client.getDestipodocident() != null) {
                TipoDocTicket tdt = new TipoDocTicket();
                tdt.setDescripcion(client.getDestipodocident());
                c.setTipoDocIdentidad(tdt);
            }
            v.setCliente(c);
        }

        // Detalle (ítems)
        List<ItemTicket> detalle = new ArrayList<>();
        if (sale.getItems() != null) {
            for (var item : sale.getItems()) {
                ItemTicket it = new ItemTicket();
                it.setDescripcion(item.getDescription());
                it.setCantidad(item.getQuantity());
                it.setPrecioUnitario(item.getUnitPrice());
                it.setSubtotal(item.getTotalAmount());
                if (item.getDiscountPercentage() != null
                        && item.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal descAmt = item.getUnitPrice()
                            .multiply(item.getQuantity())
                            .multiply(item.getDiscountPercentage())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    it.setDescuento(descAmt);
                }
                if (item.getUnitMeasure() != null) {
                    String sym = item.getUnitMeasure().getSymbol();
                    it.setUnidad(sym != null && !sym.isBlank() ? sym : item.getUnitMeasure().getCode());
                }
                detalle.add(it);
            }
        }
        v.setDetalle(detalle);

        // Totales
        v.setSubtotal(sale.getSubtotalAmount());
        v.setIgv(sale.getTaxAmount());
        v.setTotal(sale.getTotalAmount());

        // Forma de pago
        v.setFormaPago(sale.getPaymentType());

        // Pagos
        if (sale.getPayments() != null && !sale.getPayments().isEmpty()) {
            List<PagoTicket> pagos = new ArrayList<>();
            for (var p : sale.getPayments()) {
                PagoTicket pt = new PagoTicket();
                if (p.getPaymentMethod() != null) {
                    MetodoPagoTicket mpt = new MetodoPagoTicket();
                    mpt.setNombre(p.getPaymentMethod().getName());
                    pt.setMetodoPago(mpt);
                }
                pt.setMonto(p.getAmount());
                pt.setVuelto(p.getChangeAmount());
                pagos.add(pt);
            }
            v.setPagos(pagos);
        }

        // Cuotas
        if (sale.getInstallments() != null && !sale.getInstallments().isEmpty()) {
            AtomicInteger counter = new AtomicInteger(0);
            List<CuotaTicket> cuotas = sale.getInstallments().stream().map(inst -> {
                CuotaTicket ct = new CuotaTicket();
                ct.setNumeroCuota(counter.incrementAndGet());
                ct.setMonto(inst.getAmount());
                ct.setFechaVencimiento(inst.getDueDate());
                return ct;
            }).collect(Collectors.toList());
            v.setCuotas(cuotas);
        }

        // Referencia / orden de compra
        v.setOrdenCompra(sale.getPurchaseOrder());

        // Observaciones
        v.setObservaciones(sale.getObservations());

        // QR (SUNAT)
        if (doc != null && doc.getQrCode() != null && !doc.getQrCode().isBlank()) {
            v.setSunatQrString(doc.getQrCode());
            v.setSunatQrBase64(buildQrBase64(doc.getQrCode()));
        }

        return v;
    }

    private String buildLogoBase64() {
        try {
            ClassPathResource resource = new ClassPathResource("static/images/logo-bc.png");
            if (resource.exists()) {
                byte[] bytes = resource.getInputStream().readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            log.debug("Logo not found: {}", e.getMessage());
        }
        return null;
    }

    private String buildQrBase64(String qrString) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrString, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) {
            log.debug("QR generation failed: {}", e.getMessage());
            return null;
        }
    }

    // ─── Inner POJOs for Thymeleaf context ───────────────────────────────────

    @Getter @Setter
    public static class EmpresaTicket {
        private String razonSocial, nombreComercial, direccion, documento;
        private String telefono, paginaWeb, eslogan, logoBase64;
    }

    @Getter @Setter
    public static class VentaTicket {
        private TipoDocVentaTicket tipoDocumentoVenta;
        private String serie, correlativo;
        private LocalDate fecha;
        private ClienteTicket cliente;
        private List<ItemTicket> detalle;
        private BigDecimal subtotal, igv, total;
        private String formaPago;
        private List<PagoTicket> pagos;
        private List<CuotaTicket> cuotas;
        private String ordenCompra, observaciones, sunatQrString, sunatQrBase64;
    }

    @Getter @Setter
    public static class TipoDocVentaTicket {
        private String nombre;
    }

    @Getter @Setter
    public static class ClienteTicket {
        private String nombreCompleto, razonSocial, nroDocumento;
        private TipoDocTicket tipoDocIdentidad;
    }

    @Getter @Setter
    public static class TipoDocTicket {
        private String descripcion;
    }

    @Getter @Setter
    public static class ItemTicket {
        private String descripcion, unidad;
        private BigDecimal cantidad, precioUnitario, subtotal, descuento;
    }

    @Getter @Setter
    public static class PagoTicket {
        private MetodoPagoTicket metodoPago;
        private BigDecimal monto, vuelto;
    }

    @Getter @Setter
    public static class MetodoPagoTicket {
        private String nombre;
    }

    @Getter @Setter
    public static class CuotaTicket {
        private int numeroCuota;
        private BigDecimal monto;
        private LocalDate fechaVencimiento;
    }
}
