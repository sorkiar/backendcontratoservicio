package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonTypeJpaRepo extends JpaRepository<PersonType, Long> {
    List<PersonType> findByStatusOrderByNameAsc(Integer status);
}
