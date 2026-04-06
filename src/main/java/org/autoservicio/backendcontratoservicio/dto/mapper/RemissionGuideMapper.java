package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideDriverResponse;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideItemResponse;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideResponse;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuideDriver;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuideItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemissionGuideMapper {

    @Mapping(target = "clientName", ignore = true)
    @Mapping(source = "drivers", target = "drivers")
    @Mapping(source = "items", target = "items")
    RemissionGuideResponse toResponse(RemissionGuide entity);

    RemissionGuideDriverResponse toDriverResponse(RemissionGuideDriver entity);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    RemissionGuideItemResponse toItemResponse(RemissionGuideItem entity);
}
