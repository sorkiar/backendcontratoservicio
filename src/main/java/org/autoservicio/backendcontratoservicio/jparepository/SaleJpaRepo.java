package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleJpaRepo extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.unitMeasure WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);

    /** Pre-fetches documents + documentTypeSunat for a set of sales into the L1 cache. */
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.documents d LEFT JOIN FETCH d.documentTypeSunat WHERE s.id IN :ids")
    List<Sale> fetchDocumentsForSaleIds(@Param("ids") List<Long> ids);

    /** Pre-fetches items + unitMeasure + product + service for a set of sales into the L1 cache. */
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.unitMeasure LEFT JOIN FETCH i.product LEFT JOIN FETCH i.service WHERE s.id IN :ids")
    List<Sale> fetchItemsForSaleIds(@Param("ids") List<Long> ids);

    /** Pre-fetches payments + paymentMethod for a set of sales into the L1 cache. */
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.payments p LEFT JOIN FETCH p.paymentMethod WHERE s.id IN :ids")
    List<Sale> fetchPaymentsForSaleIds(@Param("ids") List<Long> ids);
}
