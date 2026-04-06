package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.*;
import org.autoservicio.backendcontratoservicio.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "clientName", ignore = true)
    @Mapping(target = "clientDocNumber", ignore = true)
    @Mapping(target = "items", source = "items")
    @Mapping(target = "payments", source = "payments")
    @Mapping(target = "documents", source = "documents")
    SaleResponse toResponse(Sale entity);

    @Mapping(source = "unitMeasure.id", target = "unitMeasureId")
    @Mapping(source = "unitMeasure.name", target = "unitMeasureName")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "service.id", target = "serviceId")
    SaleItemResponse toItemResponse(SaleItem entity);

    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    @Mapping(source = "paymentMethod.name", target = "paymentMethodName")
    SalePaymentResponse toPaymentResponse(SalePayment entity);

    @Mapping(source = "documentTypeSunat.code", target = "documentTypeCode")
    @Mapping(source = "documentTypeSunat.name", target = "documentTypeName")
    SaleDocumentResponse toDocumentResponse(SaleDocument entity);
}
