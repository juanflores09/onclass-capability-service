package com.pragma.onclass_capability_service.infraestructure.adapters.technologyvalidatoradapter.dto;

import java.util.List;

public record TechnologyValidationRequestDTO(
        List<Long> ids
) {
}
