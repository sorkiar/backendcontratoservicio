package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.UbigeoResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.Ubigeo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UbigeoMapper {
    UbigeoResponse toResponse(Ubigeo entity);
}
