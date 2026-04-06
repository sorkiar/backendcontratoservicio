package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.AuditEntity;

@Entity
@Table(name = "charge_unit")
@Getter
@Setter
public class ChargeUnit extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
