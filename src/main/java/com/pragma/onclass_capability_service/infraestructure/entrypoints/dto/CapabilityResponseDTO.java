package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import java.util.List;

public record CapabilityResponseDTO(
        Long id,
        String name,
        String description,
        List<TechnologyResponseDTO> technologies
) {
}
