package com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto;

import java.util.List;

public record CapabilityTechnologiesResponseDTO(
        Long capabilityId,
        List<TechnologyDTO> technologies
) {
}
