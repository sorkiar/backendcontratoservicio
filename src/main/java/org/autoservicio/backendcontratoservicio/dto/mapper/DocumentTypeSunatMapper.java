package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.DocumentTypeSunatResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentTypeSunat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentTypeSunatMapper {
    DocumentTypeSunatResponse toResponse(DocumentTypeSunat entity);
}
