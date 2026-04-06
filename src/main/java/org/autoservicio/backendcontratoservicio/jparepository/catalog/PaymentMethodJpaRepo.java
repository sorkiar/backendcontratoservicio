package org.autoservicio.backendcontratoservicio.jparepository.catalog;

import org.autoservicio.backendcontratoservicio.entity.catalog.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentMethodJpaRepo extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByStatusOrderByNameAsc(Integer status);
}
