package com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper;

import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.TechnologyDTO;
import org.springframework.stereotype.Component;

@Component
public class TechnologyMapper {

    public Technology technologyDtoToTechnology(TechnologyDTO dto){
        return new Technology(
                dto.id(),
                dto.name(),
                dto.description()
        );
    }
}
