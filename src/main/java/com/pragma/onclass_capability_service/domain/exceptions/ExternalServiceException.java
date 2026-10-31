package com.pragma.onclass_capability_service.domain.exceptions;

import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.ApiResponseDTO;
import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException{

    private final ApiResponseDTO errorResponse;

    public ExternalServiceException(ApiResponseDTO errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : "External service error");
        this.errorResponse = errorResponse;
    }
}
