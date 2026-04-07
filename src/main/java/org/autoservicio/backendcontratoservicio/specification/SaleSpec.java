package org.autoservicio.backendcontratoservicio.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.autoservicio.backendcontratoservicio.entity.Sale;
import org.autoservicio.backendcontratoservicio.entity.SaleDocument;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SaleSpec {

    private SaleSpec() {}

    public static Specification<Sale> build(Long clientId, String saleStatus,
                                             LocalDate startDate, LocalDate endDate,
                                             String documentStatus) {
        return Specification.allOf(
                hasClient(clientId),
                hasStatus(saleStatus),
                afterDate(startDate),
                beforeDate(endDate),
                hasDocumentStatus(documentStatus)
        );
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

    private static Specification<Sale> hasDocumentStatus(String documentStatus) {
        return (root, query, cb) -> {
            if (documentStatus == null || documentStatus.isBlank()) return null;
            query.distinct(true);
            Join<Sale, SaleDocument> docs = root.join("documents", JoinType.INNER);
            return cb.equal(docs.get("status"), documentStatus);
        };
    }
}
