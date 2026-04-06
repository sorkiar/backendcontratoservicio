package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.ServiceCategoryResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.ServiceCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceCategoryMapper {
    ServiceCategoryResponse toResponse(ServiceCategory entity);
}
