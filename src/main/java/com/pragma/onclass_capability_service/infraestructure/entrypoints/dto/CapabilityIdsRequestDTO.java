package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import java.util.List;

public record CapabilityIdsRequestDTO(
        List<Long> ids
) {
}
