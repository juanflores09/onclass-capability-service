package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

public record CapabilityWithCountDTO (
        Long id,
        String name,
        String description,
        Long techCount
){
}
