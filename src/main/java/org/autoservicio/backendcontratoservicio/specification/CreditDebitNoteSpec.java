package org.autoservicio.backendcontratoservicio.specification;

import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CreditDebitNoteSpec {

    private CreditDebitNoteSpec() {}

    public static Specification<CreditDebitNote> build(Long saleId, String status, LocalDate startDate, LocalDate endDate) {
        return Specification
                .where(hasSale(saleId))
                .and(hasStatus(status))
                .and(afterDate(startDate))
                .and(beforeDate(endDate));
    }

    private static Specification<CreditDebitNote> hasSale(Long saleId) {
        return (root, query, cb) -> {
            if (saleId == null) return null;
            return cb.equal(root.get("sale").get("id"), saleId);
        };
    }

    private static Specification<CreditDebitNote> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<CreditDebitNote> afterDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("issueDate"), startDate);
        };
    }

    private static Specification<CreditDebitNote> beforeDate(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("issueDate"), endDate);
        };
    }
}
