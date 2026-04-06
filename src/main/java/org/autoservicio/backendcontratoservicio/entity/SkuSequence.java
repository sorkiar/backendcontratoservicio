package org.autoservicio.backendcontratoservicio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sku_sequence")
@Getter
@Setter
public class SkuSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, unique = true, length = 10)
    private String type;

    @Column(name = "last_seq_value", nullable = false)
    private Integer lastSeqValue = 0;
}
