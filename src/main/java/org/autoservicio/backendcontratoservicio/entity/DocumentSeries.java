package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;

@Entity
@Table(name = "document_series")
@Getter
@Setter
public class DocumentSeries extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_sunat_code", nullable = false)
    private DocumentTypeSunat documentTypeSunat;

    @Column(name = "series", nullable = false, length = 10)
    private String series;

    @Column(name = "current_sequence", nullable = false)
    private Integer currentSequence = 0;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
