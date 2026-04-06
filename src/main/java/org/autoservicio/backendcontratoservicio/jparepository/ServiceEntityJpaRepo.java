package org.autoservicio.backendcontratoservicio.jparepository;

import org.autoservicio.backendcontratoservicio.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceEntityJpaRepo extends JpaRepository<ServiceEntity, Long>, JpaSpecificationExecutor<ServiceEntity> {
}
