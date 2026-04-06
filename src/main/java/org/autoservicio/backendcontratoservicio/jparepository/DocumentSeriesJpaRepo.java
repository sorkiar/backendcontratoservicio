package org.autoservicio.backendcontratoservicio.jparepository;

import jakarta.persistence.LockModeType;
import org.autoservicio.backendcontratoservicio.entity.DocumentSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentSeriesJpaRepo extends JpaRepository<DocumentSeries, Long> {

    List<DocumentSeries> findByStatusOrderBySeriesAsc(Integer status);

    List<DocumentSeries> findByDocumentTypeSunatCodeAndStatusOrderBySeriesAsc(String code, Integer status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DocumentSeries ds WHERE ds.id = :id")
    Optional<DocumentSeries> findByIdWithLock(@Param("id") Long id);

    Optional<DocumentSeries> findByDocumentTypeSunatCodeAndSeriesAndStatus(String code, String series, Integer status);
}
