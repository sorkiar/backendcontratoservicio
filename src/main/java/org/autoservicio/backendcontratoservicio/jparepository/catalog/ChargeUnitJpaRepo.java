package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.ChargeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeUnitJpaRepo extends JpaRepository<ChargeUnit, Long> {
    List<ChargeUnit> findByStatusOrderByNameAsc(Integer status);
}
