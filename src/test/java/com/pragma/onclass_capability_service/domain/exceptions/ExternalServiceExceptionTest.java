package com.pragma.onclass_capability_service.domain.exceptions;

import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.ApiResponseDTO;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExternalServiceExceptionTest {

    @Test
    void shouldCreateExceptionWithApiResponseDTO() {
        // Given
        ApiResponseDTO apiResponseDTO = ApiResponseDTO.builder()
                .code("400")
                .message("Invalid external service data")
                .build();

        // When
        ExternalServiceException exception = new ExternalServiceException(apiResponseDTO);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid external service data");
        assertThat(exception.getErrorResponse()).isEqualTo(apiResponseDTO);
        assertThat(exception.getErrorResponse().getCode()).isEqualTo("400");
    }

    @Test
    void shouldCreateExceptionWithDefaultMessage_WhenApiResponseDTOIsNull() {
        // When
        ExternalServiceException exception = new ExternalServiceException(null);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("External service error");
        assertThat(exception.getErrorResponse()).isNull();
    }
}
