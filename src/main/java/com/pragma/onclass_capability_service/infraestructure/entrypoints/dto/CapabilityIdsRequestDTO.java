package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CapabilityIdsRequestDTO(
        @NotEmpty(message = "La lista de IDs no puede estar vacía")
        List<Long> ids
) {
}
