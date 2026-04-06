package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.UnitMeasureResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.UnitMeasure;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitMeasureMapper {
    UnitMeasureResponse toResponse(UnitMeasure entity);
}
