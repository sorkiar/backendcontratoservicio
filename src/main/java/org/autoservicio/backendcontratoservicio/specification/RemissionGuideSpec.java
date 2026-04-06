package org.autoservicio.backendcontratoservicio.specification;

import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class RemissionGuideSpec {

    private RemissionGuideSpec() {}

    public static Specification<RemissionGuide> build(Long clientId, String status, LocalDate startDate, LocalDate endDate) {
        return Specification
                .where(hasClient(clientId))
                .and(hasStatus(status))
                .and(afterDate(startDate))
                .and(beforeDate(endDate));
    }

    private static Specification<RemissionGuide> hasClient(Long clientId) {
        return (root, query, cb) -> {
            if (clientId == null) return null;
            return cb.equal(root.get("clientId"), clientId);
        };
    }

    private static Specification<RemissionGuide> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<RemissionGuide> afterDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("issueDate"), startDate);
        };
    }

    private static Specification<RemissionGuide> beforeDate(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("issueDate"), endDate);
        };
    }
}
