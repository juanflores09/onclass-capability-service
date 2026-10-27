package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

public record CapabilityResponseDTO(
        Long id,
        String name,
        String description,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<TechnologyResponseDTO> technologies
) {

    public CapabilityResponseDTO(Long id, String name, String description) {
        this(id, name, description, Collections.emptyList());
    }
}
