package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.SunatRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SunatRequestLogJpaRepo extends JpaRepository<SunatRequestLog, Long> {
    List<SunatRequestLog> findByDocumentTypeAndDocumentIdOrderByCreatedAtDesc(String documentType, Long documentId);
}
