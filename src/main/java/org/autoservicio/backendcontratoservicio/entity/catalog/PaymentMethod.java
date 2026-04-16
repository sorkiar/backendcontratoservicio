package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autoservicio.backendcontratoservicio.entity.AuditEntity;

@Entity
@Table(name = "payment_method")
@Getter
@Setter
public class PaymentMethod extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "requires_proof", nullable = false)
    private Boolean requiresProof = false;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT")
    private Integer status = 1;
}
