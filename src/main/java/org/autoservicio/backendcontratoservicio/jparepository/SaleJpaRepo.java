package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleJpaRepo extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.unitMeasure WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);
}
