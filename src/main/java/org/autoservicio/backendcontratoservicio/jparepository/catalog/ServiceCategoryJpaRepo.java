package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryJpaRepo extends JpaRepository<ServiceCategory, Long> {
    List<ServiceCategory> findByStatusOrderByNameAsc(Integer status);
    Optional<ServiceCategory> findByIdAndStatus(Long id, Integer status);
}
