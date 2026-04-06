package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.AuditEntity;

@Entity
@Table(name = "credit_debit_note_type")
@Getter
@Setter
public class CreditDebitNoteType extends AuditEntity {

    @Id
    @Column(name = "code", length = 4)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "note_category", nullable = false, length = 10)
    private String noteCategory;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
