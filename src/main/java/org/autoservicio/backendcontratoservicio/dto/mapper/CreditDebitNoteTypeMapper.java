package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteTypeResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.CreditDebitNoteType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreditDebitNoteTypeMapper {
    CreditDebitNoteTypeResponse toResponse(CreditDebitNoteType entity);
}
