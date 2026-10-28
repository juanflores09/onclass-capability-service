package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CreateCapabilityUseCaseTest {

    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private TechnologyGateway technologyValidatorGateway;

    @InjectMocks
    private CreateCapabilityUseCase createCapabilityUseCase;

    private Capability capability;
    private String messageId;

    @BeforeEach
    void setUp() {
        messageId = "msg-12345";
        Technology tech = new Technology();
        tech.setId(1L);

        capability = new Capability();
        capability.setName("Backend Development");
        capability.setDescription("Desarrollo backend");
        capability.setTechnologies(List.of(tech));
    }

    @Test
    void createCapability_Success() {
        // Given
        when(capabilityPersistencePort.existsByName(capability.getName())).thenReturn(Mono.just(false));
        when(technologyValidatorGateway.validateTechnologiesExist(List.of(1L), messageId)).thenReturn(Mono.just(true));
        when(capabilityPersistencePort.save(capability)).thenReturn(Mono.just(capability));

        // When
        Mono<Capability> result = createCapabilityUseCase.createCapability(capability, messageId);

        // Then
        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).existsByName(capability.getName());
        verify(technologyValidatorGateway, times(1)).validateTechnologiesExist(List.of(1L), messageId);
        verify(capabilityPersistencePort, times(1)).save(capability);
    }

    @Test
    void createCapability_AlreadyExists_ThrowsBusinessException() {
        // Given
        when(capabilityPersistencePort.existsByName(capability.getName())).thenReturn(Mono.just(true));

        // When
        Mono<Capability> result = createCapabilityUseCase.createCapability(capability, messageId);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(BusinessException.class)
                            .hasMessage(TechnicalMessage.CAPABILITY_ALREADY_EXISTS.getMessage());
                })
                .verify();

        verify(capabilityPersistencePort, times(1)).existsByName(capability.getName());
        verify(technologyValidatorGateway, never()).validateTechnologiesExist(any(), any());
        verify(capabilityPersistencePort, never()).save(any());
    }

    @Test
    void createCapability_TechnologyNotFound_ThrowsBusinessException() {
        // Given
        when(capabilityPersistencePort.existsByName(capability.getName())).thenReturn(Mono.just(false));
        when(technologyValidatorGateway.validateTechnologiesExist(List.of(1L), messageId)).thenReturn(Mono.just(false));

        // When
        Mono<Capability> result = createCapabilityUseCase.createCapability(capability, messageId);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(BusinessException.class)
                            .hasMessage(TechnicalMessage.TECHNOLOGY_NOT_FOUND.getMessage());
                })
                .verify();

        verify(capabilityPersistencePort, times(1)).existsByName(capability.getName());
        verify(technologyValidatorGateway, times(1)).validateTechnologiesExist(List.of(1L), messageId);
        verify(capabilityPersistencePort, never()).save(any());
    }
}
