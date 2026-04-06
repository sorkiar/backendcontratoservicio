package org.autoservicio.backendcontratoservicio.specification;

import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SaleSpec {

    private SaleSpec() {}

    public static Specification<Sale> build(Long clientId, String saleStatus, LocalDate startDate, LocalDate endDate) {
        return Specification
                .where(hasClient(clientId))
                .and(hasStatus(saleStatus))
                .and(afterDate(startDate))
                .and(beforeDate(endDate));
    }

    private static Specification<Sale> hasClient(Long clientId) {
        return (root, query, cb) -> {
            if (clientId == null) return null;
            return cb.equal(root.get("clientId"), clientId);
        };
    }

    private static Specification<Sale> hasStatus(String saleStatus) {
        return (root, query, cb) -> {
            if (saleStatus == null || saleStatus.isBlank()) return null;
            return cb.equal(root.get("saleStatus"), saleStatus);
        };
    }

    private static Specification<Sale> afterDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("saleDate"), startDate);
        };
    }

    private static Specification<Sale> beforeDate(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("saleDate"), endDate);
        };
    }
}
