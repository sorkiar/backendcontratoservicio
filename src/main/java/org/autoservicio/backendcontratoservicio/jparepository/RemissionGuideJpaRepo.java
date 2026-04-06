package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemissionGuideJpaRepo extends JpaRepository<RemissionGuide, Long>, JpaSpecificationExecutor<RemissionGuide> {
    List<RemissionGuide> findByStatusOrderByCreatedAtDesc(String status);

    /** Carga ítems + product (una sola colección para evitar MultipleBagFetchException). */
    @Query("SELECT DISTINCT g FROM RemissionGuide g " +
           "LEFT JOIN FETCH g.items i LEFT JOIN FETCH i.product " +
           "WHERE g.id = :id")
    Optional<RemissionGuide> findByIdWithItems(@Param("id") Long id);

    /** Carga conductores (colección separada). */
    @Query("SELECT DISTINCT g FROM RemissionGuide g LEFT JOIN FETCH g.drivers WHERE g.id = :id")
    Optional<RemissionGuide> findByIdWithDrivers(@Param("id") Long id);
}
