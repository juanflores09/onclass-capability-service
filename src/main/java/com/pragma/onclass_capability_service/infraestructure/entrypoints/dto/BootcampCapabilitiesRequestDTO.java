package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BootcampCapabilitiesRequestDTO(
        @NotNull(message = "BootcampId ID cannot be null")
        Long bootcampId,

        @NotEmpty(message = "Capability IDs list cannot be empty")
        List<Long> capabilityIds
) {
}
