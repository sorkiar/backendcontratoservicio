package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentTypeSunatJpaRepo extends JpaRepository<DocumentTypeSunat, String> {
    List<DocumentTypeSunat> findByStatusOrderByNameAsc(Integer status);
}
