package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CapabilityRequestDTO(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 50, message = "El tamaño máximo del nombre debe ser de 50 caracteres")
        String name,

        @NotBlank(message = "La descripción no puede estar vacía")
        @Size(max = 90, message = "El tamaño máximo de la descripción debe ser de 90 caracteres")
        String description,

        @NotEmpty(message = "La lista no puede estar vacía")
        @Size(min = 3, max = 20, message = "La capacidad debe tener entre 3 y 20 tecnologías asociadas")
        List<Long> technologies
) {
}
