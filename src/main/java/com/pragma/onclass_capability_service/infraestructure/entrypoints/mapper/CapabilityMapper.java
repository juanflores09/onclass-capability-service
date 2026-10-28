package com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityResponseDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.TechnologyResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CapabilityMapper {

    public CapabilityEntity capabilityToCapabilityEntity(Capability capability){
        CapabilityEntity entity = new CapabilityEntity();
        entity.setName(capability.getName());
        entity.setDescription(capability.getDescription());
        return entity;
    }

    public Capability capabilityEntityToCapability(CapabilityEntity entity){
        return new Capability(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    public Capability capabilityEntityToCapability(CapabilityEntity entity, List<Technology> technologies){
        return new Capability(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                technologies
        );
    }

    public CapabilityResponseDTO capabilityResponseDTOToCapability(Capability capability){
        return new CapabilityResponseDTO(
                capability.getId(),
                capability.getName(),
                capability.getDescription()
        );
    }


    public CapabilityResponseDTO capabilityResponseDTOToCapability(Capability capability, List<Technology> technologyList){
        return new CapabilityResponseDTO(
                capability.getId(),
                capability.getName(),
                capability.getDescription(),
                technologyList
                        .stream()
                        .map(x -> new TechnologyResponseDTO(x.getId(), x.getName()))
                        .toList()
        );
    }

    public Capability capabilityRequestDTOToCapability(CapabilityRequestDTO requestDTO){
        Capability capability = new Capability();
        capability.setName(requestDTO.name());
        capability.setDescription(requestDTO.description());
        capability.setTechnologies(requestDTO.technologies().stream()
                .map(Technology::new)
                .toList());
        return capability;
    }
}
