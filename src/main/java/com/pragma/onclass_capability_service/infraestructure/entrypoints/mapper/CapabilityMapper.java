package com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapabilityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "technologies", target = "technologies")
    Capability capabilityDtoToCapability(CapabilityRequestDTO dto);

}
