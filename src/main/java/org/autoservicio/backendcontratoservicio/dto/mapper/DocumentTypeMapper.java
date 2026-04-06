package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.DocumentTypeResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentTypeMapper {
    @Mapping(source = "personType.id", target = "personTypeId")
    @Mapping(source = "personType.name", target = "personTypeName")
    DocumentTypeResponse toResponse(DocumentType entity);
}
