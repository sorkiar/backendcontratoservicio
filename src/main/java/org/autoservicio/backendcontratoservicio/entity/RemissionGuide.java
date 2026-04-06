package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "remission_guide")
@Getter
@Setter
public class RemissionGuide extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id")
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_series_id")
    private DocumentSeries documentSeries;

    @Column(name = "series", length = 10)
    private String series;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "transfer_reason", length = 50)
    private String transferReason;

    @Column(name = "transfer_reason_description", length = 255)
    private String transferReasonDescription;

    @Column(name = "transport_mode", length = 20)
    private String transportMode;

    @Column(name = "gross_weight", precision = 12, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit;

    @Column(name = "package_count")
    private Integer packageCount;

    @Column(name = "origin_address", length = 500)
    private String originAddress;

    @Column(name = "origin_ubigeo", length = 10)
    private String originUbigeo;

    @Column(name = "destination_address", length = 500)
    private String destinationAddress;

    @Column(name = "destination_ubigeo", length = 10)
    private String destinationUbigeo;

    @Column(name = "minor_vehicle_transfer")
    private Boolean minorVehicleTransfer = false;

    @Column(name = "carrier_ruc", length = 20)
    private String carrierRuc;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Column(name = "carrier_authorization_code", length = 50)
    private String carrierAuthorizationCode;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDIENTE";

    @Column(name = "sunat_response_code", length = 10)
    private String sunatResponseCode;

    @Column(name = "sunat_message", length = 500)
    private String sunatMessage;

    @Column(name = "hash_code", length = 500)
    private String hashCode;

    @Column(name = "qr_code", length = 1000)
    private String qrCode;

    @Column(name = "xml_base64", columnDefinition = "LONGTEXT")
    private String xmlBase64;

    @Column(name = "cdr_base64", columnDefinition = "LONGTEXT")
    private String cdrBase64;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "xml_url", length = 500)
    private String xmlUrl;

    @Column(name = "cdr_url", length = 500)
    private String cdrUrl;

    @OneToMany(mappedBy = "remissionGuide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RemissionGuideDriver> drivers = new ArrayList<>();

    @OneToMany(mappedBy = "remissionGuide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RemissionGuideItem> items = new ArrayList<>();
}
