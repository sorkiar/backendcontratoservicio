package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.AuditEntity;

@Entity
@Table(name = "document_type")
@Getter
@Setter
public class DocumentType extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_type_id", nullable = false)
    private PersonType personType;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "length")
    private Integer length;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
