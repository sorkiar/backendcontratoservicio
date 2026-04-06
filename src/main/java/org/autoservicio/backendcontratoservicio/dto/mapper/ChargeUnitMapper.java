package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.ChargeUnitResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.ChargeUnit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeUnitMapper {
    ChargeUnitResponse toResponse(ChargeUnit entity);
}
