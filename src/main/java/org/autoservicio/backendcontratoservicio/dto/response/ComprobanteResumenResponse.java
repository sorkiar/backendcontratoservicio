package org.autoservicio.backendcontratoservicio.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ComprobanteResumenResponse {
    /** "VENTA_DOC" | "NOTA" | "GUIA" */
    private String tipoEntidad;
    private Long id;
    private String tipoDocumentoCodigo;
    private String tipoDocumentoNombre;
    private String serie;
    private Integer correlativo;
    private LocalDate fechaEmision;
    private String status;
    private String sunatResponseCode;
    private String sunatMessage;
    private String pdfUrl;
    /** Presente en VENTA_DOC y NOTA */
    private Long saleId;
    /** Presente en GUIA */
    private Long clientId;
}
