package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleDocumentJpaRepo extends JpaRepository<SaleDocument, Long>, JpaSpecificationExecutor<SaleDocument> {
    List<SaleDocument> findByStatusOrderByCreatedAtDesc(String status);
    List<SaleDocument> findBySaleIdOrderByCreatedAtDesc(Long saleId);

    @Query("SELECT d FROM SaleDocument d LEFT JOIN FETCH d.documentTypeSunat WHERE d.id = :id")
    Optional<SaleDocument> findByIdWithType(@Param("id") Long id);

    @Query("SELECT d FROM SaleDocument d LEFT JOIN FETCH d.documentTypeSunat LEFT JOIN FETCH d.sale WHERE d.id = :id")
    Optional<SaleDocument> findByIdWithSale(@Param("id") Long id);
}
