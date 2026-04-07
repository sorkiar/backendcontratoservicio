package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.Ubigeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbigeoJpaRepo extends JpaRepository<Ubigeo, String> {
    List<Ubigeo> findAllByOrderByDepartmentAscProvinceAscDistritAsc();
    List<Ubigeo> findByStatusOrderByDepartmentAscProvinceAscDistritAsc(Integer status);
}
