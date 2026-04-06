package org.autoservicio.backendcontratoservicio.specification;

import org.autoservicio.backendcontratoservicio.entity.ServiceEntity;
import org.springframework.data.jpa.domain.Specification;

public class ServiceEntitySpec {

    private ServiceEntitySpec() {}

    public static Specification<ServiceEntity> build(String name, String sku, Long serviceCategoryId, Integer status) {
        return Specification
                .where(hasName(name))
                .and(hasSku(sku))
                .and(hasServiceCategory(serviceCategoryId))
                .and(hasStatus(status));
    }

    private static Specification<ServiceEntity> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    private static Specification<ServiceEntity> hasSku(String sku) {
        return (root, query, cb) -> {
            if (sku == null || sku.isBlank()) return null;
            return cb.like(root.get("sku"), "%" + sku + "%");
        };
    }

    private static Specification<ServiceEntity> hasServiceCategory(Long serviceCategoryId) {
        return (root, query, cb) -> {
            if (serviceCategoryId == null) return null;
            return cb.equal(root.get("serviceCategory").get("id"), serviceCategoryId);
        };
    }

    private static Specification<ServiceEntity> hasStatus(Integer status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }
}
