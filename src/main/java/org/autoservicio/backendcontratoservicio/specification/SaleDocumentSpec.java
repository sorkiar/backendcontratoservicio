package org.autoservicio.backendcontratoservicio.specification;

import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SaleDocumentSpec {

    private SaleDocumentSpec() {}

    public static Specification<SaleDocument> build(String status, String docTypeCode, LocalDate startDate, LocalDate endDate) {
        return Specification
                .where(hasStatus(status))
                .and(hasDocType(docTypeCode))
                .and(afterDate(startDate))
                .and(beforeDate(endDate));
    }

    private static Specification<SaleDocument> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<SaleDocument> hasDocType(String docTypeCode) {
        return (root, query, cb) -> {
            if (docTypeCode == null || docTypeCode.isBlank()) return null;
            return cb.equal(root.get("documentTypeSunat").get("code"), docTypeCode);
        };
    }

    private static Specification<SaleDocument> afterDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("issueDate"), startDate);
        };
    }

    private static Specification<SaleDocument> beforeDate(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("issueDate"), endDate);
        };
    }
}
