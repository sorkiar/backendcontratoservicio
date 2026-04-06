package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.UnitMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UnitMeasureJpaRepo extends JpaRepository<UnitMeasure, Long> {
    List<UnitMeasure> findByStatusOrderByNameAsc(Integer status);
}
