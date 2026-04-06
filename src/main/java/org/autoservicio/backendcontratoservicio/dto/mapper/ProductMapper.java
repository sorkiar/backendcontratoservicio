package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.ProductResponse;
import org.autoservicio.backendcontratoservicio.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "unitMeasure.id", target = "unitMeasureId")
    @Mapping(source = "unitMeasure.name", target = "unitMeasureName")
    ProductResponse toResponse(Product entity);
}
