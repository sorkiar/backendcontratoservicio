package org.autoservicio.backendcontratoservicio.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.interfaces.IDocumentoRepo;
import org.autoservicio.backendcontratoservicio.model.documentoModel;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ContratosRepository;
import org.autoservicio.backendcontratoservicio.response.FacturacionResponse;
import org.autoservicio.backendcontratoservicio.response.ServicioContratadoRequest;
import org.autoservicio.backendcontratoservicio.util.ConfiguracionUtil;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoJobSerivce {
    private final IDocumentoRepo documentoRepository;
    private final ContratosRepository contratoRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConfiguracionUtil configuracionUtil;

    @Scheduled(fixedRate = 1800000) // 1800000 cada 30 minutos
    public void enviarDocumentosPendientes() {
        List<documentoModel> pendientes = documentoRepository.findByEstado("PENDIENTE");

        for (documentoModel doc : pendientes) {
            try {
                List<ServicioContratadoRequest> productos = contratoRepository.buscar_servicio_x_codigo_factura(doc.getCodigoFactura());
                if (productos.isEmpty()) continue;


                Map<String, Object> requestJson = construirJsonFacturacion(doc, productos);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestJson, headers);
                String url = "https://magistrack-bc.website/facturador-magistrack/facturador/sunat/comprobante/sent";

                //System.out.println("Enviando comprobante:");
                //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestJson));


                ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        FacturacionResponse.class
                );

                FacturacionResponse factResponse = response.getBody();

                if (response.getStatusCode().is2xxSuccessful() && factResponse != null && factResponse.isSuccess()) {
                    //System.out.println("Respuesta del facturador:");
                    //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody()));
                    var data = factResponse.getData();
                    if ("ACEPTADO".equalsIgnoreCase(data.getTypeResultadoDeclaracion())) {
                        doc.setEstado("EMITIDO");
                    } else {
                        doc.setEstado("ERROR");
                    }

                    doc.setMensaje(data.getMessage());
                    doc.setResponseCode(data.getResponseCode());
                    doc.setCodigoHash(data.getCodigoHash());
                    doc.setCadenaQr(data.getCadenaQr());
                    doc.setXmlBase64(data.getBase64Xml());
                    doc.setXmlCdrBase64(data.getBase64XmlCdr());
                    doc.setFechaEmision(LocalDateTime.now());
                } else {
                    doc.setEstado("ERROR");
                    String errorMsg = response.getStatusCode().toString();
                    if (factResponse != null) {
                        errorMsg += " - " + factResponse.getMessage();
                    }
                    doc.setMensaje("Error al enviar: " + errorMsg);
                }

            } catch (Exception e) {
                doc.setEstado("ERROR");
                doc.setMensaje("Error al enviar: " + e.getMessage());
                log.error("Error al enviar documento ID {}: {}", doc.getIdDocumento(), e.getMessage(), e);
            }

            documentoRepository.save(doc);
        }
    }

    private Map<String, Object> construirJsonFacturacion(documentoModel doc,  List<ServicioContratadoRequest> productos) {
        Map<String, Object> json = new LinkedHashMap<>();

        Map<String, String> empresaConfig = configuracionUtil.obtenerGrupo("empresa_emisora");

        Map<String, Object> empresa = new LinkedHashMap<>();
        empresa.put("emprRuc", empresaConfig.get("emprRuc"));
        empresa.put("emprRazonSocial", empresaConfig.get("emprRazonSocial"));
        empresa.put("emprDireccionFiscal", empresaConfig.get("emprDireccionFiscal"));
        empresa.put("emprCodigoEstablecimientoSunat", empresaConfig.get("emprCodigoEstablecimientoSunat"));
        empresa.put("ubigeo", Map.of(
                "ubigUbigeo", empresaConfig.get("ubigUbigeo"),
                "ubigDepartamento", empresaConfig.get("ubigDepartamento"),
                "ubigProvincia", empresaConfig.get("ubigProvincia"),
                "ubigDistrito", empresaConfig.get("ubigDistrito")
        ));
        empresa.put("emprLeyAmazonia", Boolean.parseBoolean(empresaConfig.get("emprLeyAmazonia")));
        empresa.put("emprProduccion", Boolean.parseBoolean(empresaConfig.get("emprProduccion")));
        empresa.put("emprCertificadoLLavePublica", empresaConfig.get("emprCertificadoLlavePublica"));
        empresa.put("emprCertificadoLLavePrivada", empresaConfig.get("emprCertificadoLlavePrivada"));
        empresa.put("emprUsuarioSecundario", empresaConfig.get("emprUsuarioSecundario"));
        empresa.put("emprClaveUsuarioSecundario", empresaConfig.get("emprClaveUsuarioSecundario"));

        json.put("empresa", empresa);

        Map<String, Object> comprobante = new LinkedHashMap<>();
        comprobante.put("tipoDocumento", doc.getTipoComprobante().equals("01") ? "FACTURA" : "BOLETA");
        comprobante.put("compSerie", doc.getSerie());
        comprobante.put("compNumero", Integer.parseInt(doc.getCorrelativo()));
        comprobante.put("moneda", "PEN");
        comprobante.put("compFechaEmision", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now()));
        comprobante.put("compTotal", calcularTotalComprobante(productos));

        // Cliente
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("clieNumeroDocumento", doc.getNumeroDocumentoCliente());
        cliente.put("clieRazonSocial", doc.getTipoComprobante().equals("01") ? doc.getRazonSocialCliente() : doc.getNombreCliente());
        cliente.put("tipoDocumentoIdentidad", doc.getTipoDocumentoCliente().equals("6") ? "RUC" : "DNI");
        comprobante.put("cliente", cliente);

        // Ítems
        List<Map<String, Object>> items = new ArrayList<>();
        for (ServicioContratadoRequest pp : productos) {
            var prod = pp.getId_contrato();

            BigDecimal cantidad = BigDecimal.ONE;

            // convertir Double a BigDecimal de manera segura
            BigDecimal precioUnitario = BigDecimal.valueOf(pp.getPrecio_mensual()); // con IGV

            BigDecimal valorUnitario = precioUnitario
                    .divide(BigDecimal.valueOf(1.18), 6, RoundingMode.HALF_UP);

            BigDecimal subtotal = valorUnitario.multiply(cantidad);
            BigDecimal igv = subtotal.multiply(BigDecimal.valueOf(0.18));
            BigDecimal total = subtotal.add(igv);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itcoUnidadMedida", "NIU");
            item.put("itcoDescripcion", pp.getPlan());
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

        comprobante.put("lsItemComprobante", items);
        json.put("comprobante", comprobante);
        return json;
    }

    private BigDecimal calcularTotalComprobante(List<ServicioContratadoRequest> productos) {
        return productos.stream()
                .map(pp -> BigDecimal.valueOf(pp.getPrecio_mensual())) // conversión de Double a BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

}