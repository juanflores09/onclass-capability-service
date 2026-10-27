package com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapabilityMapper {

    Capability entityToDomain(CapabilityEntity entity);

}
