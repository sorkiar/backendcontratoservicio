package org.autoservicio.backendcontratoservicio.jparepository;

import jakarta.persistence.LockModeType;
import org.autoservicio.backendcontratoservicio.entity.SkuSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkuSequenceJpaRepo extends JpaRepository<SkuSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SkuSequence s WHERE s.type = :type")
    Optional<SkuSequence> findByTypeWithLock(@Param("type") String type);

    Optional<SkuSequence> findByType(String type);
}
