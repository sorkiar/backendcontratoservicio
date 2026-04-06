package org.autoservicio.backendcontratoservicio.specification;

import org.autoservicio.backendcontratoservicio.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpec {

    private ProductSpec() {}

    public static Specification<Product> build(String name, String sku, Long categoryId, Integer status) {
        return Specification
                .where(hasName(name))
                .and(hasSku(sku))
                .and(hasCategory(categoryId))
                .and(hasStatus(status));
    }

    private static Specification<Product> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    private static Specification<Product> hasSku(String sku) {
        return (root, query, cb) -> {
            if (sku == null || sku.isBlank()) return null;
            return cb.like(root.get("sku"), "%" + sku + "%");
        };
    }

    private static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    private static Specification<Product> hasStatus(Integer status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }
}
