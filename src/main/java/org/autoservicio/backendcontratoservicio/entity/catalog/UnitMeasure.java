package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.AuditEntity;

@Entity
@Table(name = "unit_measure")
@Getter
@Setter
public class UnitMeasure extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "code_sunat", nullable = false, length = 10)
    private String codeSunat;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", length = 20)
    private String symbol;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
