package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import java.util.List;

public record PageResponseDTO<T>(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        List<T> content
) {
}
