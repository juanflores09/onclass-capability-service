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
        capability.setId(1L);
        capability.setName("Backend Development");
        capability.setDescription("Desarrollo backend");
        capability.setTechnologies(List.of(tech));
    }

    @Test
    void createCapability_Success() {
        // Given
        when(capabilityPersistencePort.existsByName(capability.getName())).thenReturn(Mono.just(false));
        when(capabilityPersistencePort.save(capability)).thenReturn(Mono.just(capability));
        when(technologyValidatorGateway.saveCapabilityTechnologies(anyLong(), anyList(), eq(messageId)))
                .thenReturn(Mono.empty());

        // When
        Mono<Capability> result = createCapabilityUseCase.createCapability(capability, messageId);

        // Then
        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).existsByName(capability.getName());
        verify(capabilityPersistencePort, times(1)).save(capability);
        verify(technologyValidatorGateway, times(1)).saveCapabilityTechnologies(any(), any(), eq(messageId));
    }

    @Test
    void createCapability_Technologies_is_Null(){

        capability.setTechnologies(null);

        when(capabilityPersistencePort.existsByName(capability.getName())).thenReturn(Mono.just(false));
        when(capabilityPersistencePort.save(capability)).thenReturn(Mono.just(capability));
        when(technologyValidatorGateway.saveCapabilityTechnologies(any(), any(), eq(messageId)))
                .thenReturn(Mono.empty());

        Mono<Capability> result = createCapabilityUseCase.createCapability(capability, messageId);

        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(capabilityPersistencePort).existsByName(capability.getName());
        verify(capabilityPersistencePort).save(capability);
        verify(technologyValidatorGateway).saveCapabilityTechnologies(any(), any(), eq(messageId));
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
        verify(capabilityPersistencePort, never()).save(any());
    }

    @Test
    void createCapability_TechnologyNotFound_ThrowsBusinessException() {
        // Given
        when(capabilityPersistencePort.existsByName(capability.getName())).thenReturn(Mono.just(false));
        when(capabilityPersistencePort.save(capability)).thenReturn(Mono.just(capability));
        when(capabilityPersistencePort.deleteById(any())).thenReturn(Mono.empty());
        when(technologyValidatorGateway.saveCapabilityTechnologies(any(), any(), eq(messageId)))
                .thenReturn(Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_FOUND)));
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
        verify(capabilityPersistencePort, times(1)).save(capability);
        verify(capabilityPersistencePort, times(1)).deleteById(any());
        verify(technologyValidatorGateway, times(1)).saveCapabilityTechnologies(any(), any(), eq(messageId));
    }
}
