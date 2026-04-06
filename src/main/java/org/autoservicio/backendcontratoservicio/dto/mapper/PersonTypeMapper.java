package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.PersonTypeResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.PersonType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonTypeMapper {
    PersonTypeResponse toResponse(PersonType entity);
}
