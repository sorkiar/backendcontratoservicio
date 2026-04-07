package org.autoservicio.backendcontratoservicio.entity.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ubigeo")
@Getter
@Setter
public class Ubigeo {

    @Id
    @Column(name = "ubigeo", length = 8)
    private String ubigeo;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "distrit", length = 50)
    private String distrit;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
