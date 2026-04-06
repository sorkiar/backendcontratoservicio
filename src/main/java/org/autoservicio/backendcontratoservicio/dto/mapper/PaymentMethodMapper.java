package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.PaymentMethodResponse;
import org.autoservicio.backendcontratoservicio.entity.catalog.PaymentMethod;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {
    PaymentMethodResponse toResponse(PaymentMethod entity);
}
