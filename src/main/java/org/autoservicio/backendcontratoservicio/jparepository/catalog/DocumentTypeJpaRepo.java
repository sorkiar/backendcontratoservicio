package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentTypeJpaRepo extends JpaRepository<DocumentType, Long> {
    List<DocumentType> findByStatusOrderByNameAsc(Integer status);
    List<DocumentType> findByPersonTypeIdAndStatusOrderByNameAsc(Long personTypeId, Integer status);
}
