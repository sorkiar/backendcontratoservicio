package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.CreditDebitNoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CreditDebitNoteTypeJpaRepo extends JpaRepository<CreditDebitNoteType, String> {
    List<CreditDebitNoteType> findByStatusOrderByNameAsc(Integer status);
    List<CreditDebitNoteType> findByNoteCategoryAndStatusOrderByNameAsc(String noteCategory, Integer status);
}
