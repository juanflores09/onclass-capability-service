package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.spi.BootcampCapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCapabilitiesByBootcampUseCaseTest {

    @Mock
    private BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort;

    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private TechnologyGateway technologyGateway;

    @InjectMocks
    private DeleteCapabilitiesByBootcampUseCase useCase;

    private final Long bootcampId = 1L;
    private final String messageId = "msg-12345";

    @Test
    @DisplayName("Debe eliminar relaciones, capacidades huérfanas y propagar al servicio de tecnologías cuando el conteo es 0")
    void shouldDeleteCapabilitiesAndPropagateToTechnologiesWhenCountIsZero() {
        // Arrange
        Long capabilityId = 10L;

        when(bootcampCapabilityPersistencePort.findCapabilitiesByBootcampId(bootcampId))
                .thenReturn(Flux.just(capabilityId));

        when(bootcampCapabilityPersistencePort.deleteByBootcampId(bootcampId))
                .thenReturn(Mono.empty());

        when(bootcampCapabilityPersistencePort.countBootcampsByCapabilityId(capabilityId))
                .thenReturn(Mono.just(0L)); // Ningún otro bootcamp la usa

        when(capabilityPersistencePort.deleteById(capabilityId))
                .thenReturn(Mono.empty());

        when(technologyGateway.deleteTechnologiesByCapability(capabilityId, messageId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.deleteCapabilitiesByBootcamp(bootcampId, messageId))
                .verifyComplete();

        // Verify
        verify(bootcampCapabilityPersistencePort).findCapabilitiesByBootcampId(bootcampId);
        verify(bootcampCapabilityPersistencePort).deleteByBootcampId(bootcampId);
        verify(bootcampCapabilityPersistencePort).countBootcampsByCapabilityId(capabilityId);
        verify(capabilityPersistencePort).deleteById(capabilityId);
        verify(technologyGateway).deleteTechnologiesByCapability(capabilityId, messageId);
    }

    @Test
    @DisplayName("Debe eliminar la relación pero conservar la capacidad y tecnologías si otro bootcamp la sigue usando (conteo > 0)")
    void shouldKeepCapabilityWhenUsedByOtherBootcamps() {
        // Arrange
        Long capabilityId = 10L;

        when(bootcampCapabilityPersistencePort.findCapabilitiesByBootcampId(bootcampId))
                .thenReturn(Flux.just(capabilityId));

        when(bootcampCapabilityPersistencePort.deleteByBootcampId(bootcampId))
                .thenReturn(Mono.empty());

        when(bootcampCapabilityPersistencePort.countBootcampsByCapabilityId(capabilityId))
                .thenReturn(Mono.just(2L)); // Otro bootcamp la sigue usando

        // Act & Assert
        StepVerifier.create(useCase.deleteCapabilitiesByBootcamp(bootcampId, messageId))
                .verifyComplete();

        // Verify
        verify(bootcampCapabilityPersistencePort).findCapabilitiesByBootcampId(bootcampId);
        verify(bootcampCapabilityPersistencePort).deleteByBootcampId(bootcampId);
        verify(bootcampCapabilityPersistencePort).countBootcampsByCapabilityId(capabilityId);

        // Aseguramos que NUNCA se intente borrar la capacidad ni llamar al gateway de tecnologías
        verify(capabilityPersistencePort, never()).deleteById(anyLong());
        verify(technologyGateway, never()).deleteTechnologiesByCapability(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe finalizar sin hacer nada si el bootcamp no tiene capacidades asociadas")
    void shouldDoNothingWhenBootcampHasNoCapabilities() {
        // Arrange
        when(bootcampCapabilityPersistencePort.findCapabilitiesByBootcampId(bootcampId))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(useCase.deleteCapabilitiesByBootcamp(bootcampId, messageId))
                .verifyComplete();

        // Verify
        verify(bootcampCapabilityPersistencePort).findCapabilitiesByBootcampId(bootcampId);
        verify(bootcampCapabilityPersistencePort, never()).deleteByBootcampId(anyLong());
        verify(capabilityPersistencePort, never()).deleteById(anyLong());
        verify(technologyGateway, never()).deleteTechnologiesByCapability(anyLong(), anyString());
    }
}
