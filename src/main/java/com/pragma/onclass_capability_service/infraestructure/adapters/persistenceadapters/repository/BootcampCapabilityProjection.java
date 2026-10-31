package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository;

public record BootcampCapabilityProjection (
        Long bootcampId,
        Long id,
        String name,
        String description
) {
}
