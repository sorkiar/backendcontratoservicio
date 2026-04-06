package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.AuditEntity;

@Entity
@Table(name = "document_type_sunat")
@Getter
@Setter
public class DocumentTypeSunat extends AuditEntity {

    @Id
    @Column(name = "code", length = 3)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
