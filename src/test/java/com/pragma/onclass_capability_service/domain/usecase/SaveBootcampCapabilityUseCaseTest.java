package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.domain.spi.BootcampCapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveBootcampCapabilityUseCaseTest {

    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private BootcampCapabilityPersistencePort capabilityTechnologyPersistencePort;

    @InjectMocks
    private SaveBootcampCapabilityUseCase useCase;

    @Test
    void saveAll_ShouldReturnEmpty_WhenCapabilityIdsIsNull() {
        // Given
        Long bootcampId = 1L;

        // When & Then
        StepVerifier.create(useCase.saveAll(bootcampId, null))
                .verifyComplete();

        verifyNoInteractions(capabilityPersistencePort);
        verifyNoInteractions(capabilityTechnologyPersistencePort);
    }

    @Test
    void saveAll_ShouldReturnEmpty_WhenCapabilityIdsIsEmpty() {
        // Given
        Long bootcampId = 1L;

        // When & Then
        StepVerifier.create(useCase.saveAll(bootcampId, Collections.emptyList()))
                .verifyComplete();

        verifyNoInteractions(capabilityPersistencePort);
        verifyNoInteractions(capabilityTechnologyPersistencePort);
    }

    @Test
    void saveAll_ShouldSaveSuccessfully_WhenAllCapabilitiesExist() {
        // Given
        Long bootcampId = 1L;
        List<Long> capabilityIds = List.of(10L, 20L);

        when(capabilityPersistencePort.areAllCapabilitiesExist(capabilityIds)).thenReturn(Mono.just(true));
        when(capabilityTechnologyPersistencePort.saveAll(bootcampId, capabilityIds)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.saveAll(bootcampId, capabilityIds))
                .verifyComplete();

        verify(capabilityPersistencePort).areAllCapabilitiesExist(capabilityIds);
        verify(capabilityTechnologyPersistencePort).saveAll(bootcampId, capabilityIds);
    }

    @Test
    void saveAll_ShouldThrowBusinessException_WhenCapabilitiesDoNotExist() {
        // Given
        Long bootcampId = 1L;
        List<Long> capabilityIds = List.of(10L, 999L);

        when(capabilityPersistencePort.areAllCapabilitiesExist(capabilityIds)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(useCase.saveAll(bootcampId, capabilityIds))
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(BusinessException.class)
                            .hasMessage(TechnicalMessage.TECHNOLOGY_NOT_FOUND.getMessage());
                })
                .verify();

        verify(capabilityPersistencePort).areAllCapabilitiesExist(capabilityIds);
        verifyNoInteractions(capabilityTechnologyPersistencePort);
    }

}
