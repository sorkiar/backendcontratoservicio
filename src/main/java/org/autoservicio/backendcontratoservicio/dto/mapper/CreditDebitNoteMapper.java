package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteItemResponse;
import org.autoservicio.backendcontratoservicio.dto.response.CreditDebitNoteResponse;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNote;
import org.autoservicio.backendcontratoservicio.entity.CreditDebitNoteItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditDebitNoteMapper {

    @Mapping(source = "sale.id", target = "saleId")
    @Mapping(source = "originalDocument.id", target = "originalDocumentId")
    @Mapping(source = "documentTypeSunat.code", target = "documentTypeCode")
    @Mapping(source = "creditDebitNoteType.code", target = "creditDebitNoteTypeCode")
    @Mapping(source = "creditDebitNoteType.name", target = "creditDebitNoteTypeName")
    @Mapping(target = "originalDocumentNumber", ignore = true)
    @Mapping(source = "items", target = "items")
    CreditDebitNoteResponse toResponse(CreditDebitNote entity);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "unitMeasure.id", target = "unitMeasureId")
    CreditDebitNoteItemResponse toItemResponse(CreditDebitNoteItem entity);
}
