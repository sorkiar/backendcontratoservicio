package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepo extends JpaRepository<Category, Long> {
    List<Category> findByStatusOrderByNameAsc(Integer status);
    Optional<Category> findByIdAndStatus(Long id, Integer status);
}
