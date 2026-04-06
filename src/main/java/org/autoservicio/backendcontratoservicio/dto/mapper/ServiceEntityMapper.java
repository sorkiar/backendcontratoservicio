package org.autoservicio.backendcontratoservicio.dto.mapper;

import org.autoservicio.backendcontratoservicio.dto.response.ServiceResponse;
import org.autoservicio.backendcontratoservicio.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceEntityMapper {
    @Mapping(source = "serviceCategory.id", target = "serviceCategoryId")
    @Mapping(source = "serviceCategory.name", target = "serviceCategoryName")
    @Mapping(source = "chargeUnit.id", target = "chargeUnitId")
    @Mapping(source = "chargeUnit.name", target = "chargeUnitName")
    ServiceResponse toResponse(ServiceEntity entity);
}
