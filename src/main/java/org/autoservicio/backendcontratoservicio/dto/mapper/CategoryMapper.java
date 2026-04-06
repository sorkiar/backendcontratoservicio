package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.CategoryResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category entity);
}
