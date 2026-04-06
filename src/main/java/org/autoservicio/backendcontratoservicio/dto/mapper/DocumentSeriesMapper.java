package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.DocumentSeriesResponse;
import org.autoservicio.backendcontratoservicio.entity.DocumentSeries;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentSeriesMapper {
    @Mapping(source = "documentTypeSunat.code", target = "documentTypeSunatCode")
    @Mapping(source = "documentTypeSunat.name", target = "documentTypeSunatName")
    DocumentSeriesResponse toResponse(DocumentSeries entity);
}
