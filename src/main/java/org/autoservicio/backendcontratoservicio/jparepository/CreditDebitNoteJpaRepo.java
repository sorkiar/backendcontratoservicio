package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditDebitNoteJpaRepo extends JpaRepository<CreditDebitNote, Long>, JpaSpecificationExecutor<CreditDebitNote> {
    List<CreditDebitNote> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT n FROM CreditDebitNote n " +
           "LEFT JOIN FETCH n.items i LEFT JOIN FETCH i.unitMeasure " +
           "LEFT JOIN FETCH n.originalDocument od LEFT JOIN FETCH od.documentTypeSunat " +
           "LEFT JOIN FETCH n.documentTypeSunat " +
           "LEFT JOIN FETCH n.sale s " +
           "LEFT JOIN FETCH n.creditDebitNoteType " +
           "WHERE n.id = :id")
    Optional<CreditDebitNote> findByIdWithDetails(@Param("id") Long id);
}
