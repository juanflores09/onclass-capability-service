package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityIdsRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityTechnologiesResponseDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.TechnologyDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.TechnologyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ListCapabilitiesUseCaseTest {
    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private TechnologyGateway technologyGateway;

    @Mock
    private TechnologyMapper technologyMapper;

    @InjectMocks
    private ListCapabilitiesUseCase useCase;

    @Test
    void areAllCapabilitiesExist_ShouldReturnTrue_WhenIdsExist() {
        // Given
        List<Long> ids = List.of(1L, 2L);
        when(capabilityPersistencePort.areAllCapabilitiesExist(ids)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(useCase.areAllCapabilitiesExist(ids))
                .expectNext(true)
                .verifyComplete();

        verify(capabilityPersistencePort).areAllCapabilitiesExist(ids);
    }

    @Test
    void listCapabilities_ShouldReturnPagedResultWithEnrichedTechnologies() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";
        String message = "msg-123";

        Capability capability = new Capability();
        capability.setId(1L);
        capability.setName("Backend");
        capability.setDescription("Backend development");

        TechnologyDTO techDTO = new TechnologyDTO(10L, "Java", "Programming language");
        CapabilityTechnologiesResponseDTO techResponse = new CapabilityTechnologiesResponseDTO(1L, List.of(techDTO));

        when(capabilityPersistencePort.findAllPagedAndSorted(eq(sortBy), eq(direction), eq(size), anyLong()))
                .thenReturn(Flux.just(capability));
        when(capabilityPersistencePort.countTotal()).thenReturn(Mono.just(1L));

        when(technologyGateway.getTechnologiesByCapabilityId(any(CapabilityIdsRequestDTO.class), eq(message)))
                .thenReturn(Flux.just(techResponse));

        // When & Then
        StepVerifier.create(useCase.listCapabilities(page, size, sortBy, direction, message))
                .assertNext(pageResult -> {
                    assert pageResult.totalElements() == 1L;
                    assert pageResult.content().size() == 1;
                    assert pageResult.content().getFirst().getId().equals(1L);
                })
                .verifyComplete();

        verify(capabilityPersistencePort).findAllPagedAndSorted(sortBy, direction, size, 0L);
        verify(capabilityPersistencePort).countTotal();
        verify(technologyGateway).getTechnologiesByCapabilityId(any(CapabilityIdsRequestDTO.class), eq(message));
    }

    @Test
    void findCapabilitiesByBootcampIds_ShouldReturnEnrichedBootcampCapabilities() {
        // Given
        List<Long> bootcampIds = List.of(100L);
        String messageId = "msg-456";

        Capability capability = new Capability(1L, "Backend", "Backend development");
        CapabilityBootcamp bootcamp = new CapabilityBootcamp(100L, List.of(capability));

        TechnologyDTO techDTO = new TechnologyDTO(10L, "Spring Boot", "Framework");
        Technology technology = new Technology(10L, "Spring Boot", "Framework");
        CapabilityTechnologiesResponseDTO techResponse = new CapabilityTechnologiesResponseDTO(1L, List.of(techDTO));

        when(capabilityPersistencePort.findCapabilitiesByBootcampIds(bootcampIds))
                .thenReturn(Flux.just(bootcamp));

        when(technologyGateway.getTechnologiesByCapabilityId(any(CapabilityIdsRequestDTO.class), eq(messageId)))
                .thenReturn(Flux.just(techResponse));

        when(technologyMapper.technologyDtoToTechnology(techDTO)).thenReturn(technology);

        // When & Then
        StepVerifier.create(useCase.findCapabilitiesByBootcampIds(bootcampIds, messageId))
                .assertNext(result -> {
                    assert result.bootcampId().equals(100L);
                    assert result.capabilities().size() == 1;
                    assert result.capabilities().getFirst().getTechnologies().size() == 1;
                    assert result.capabilities().getFirst().getTechnologies().getFirst().getName().equals("Spring Boot");
                })
                .verifyComplete();

        verify(capabilityPersistencePort).findCapabilitiesByBootcampIds(bootcampIds);
        verify(technologyGateway).getTechnologiesByCapabilityId(any(CapabilityIdsRequestDTO.class), eq(messageId));
        verify(technologyMapper).technologyDtoToTechnology(techDTO);
    }

    @Test
    void listCapabilities_ShouldReturnEmptyPageResult_WhenNoCapabilitiesFound() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";
        String message = "msg-123";
        long offset = 0L;

        when(capabilityPersistencePort.findAllPagedAndSorted(sortBy, direction, size, offset))
                .thenReturn(Flux.empty());

        when(capabilityPersistencePort.countTotal())
                .thenReturn(Mono.just(0L));

        // When & Then
        StepVerifier.create(useCase.listCapabilities(page, size, sortBy, direction, message))
                .assertNext(pageResult -> {
                    assertThat(pageResult.content()).isEmpty();
                    assertThat(pageResult.totalElements()).isZero();
                })
                .verifyComplete();

        verify(capabilityPersistencePort).findAllPagedAndSorted(sortBy, direction, size, offset);
        verify(capabilityPersistencePort).countTotal();
        verifyNoInteractions(technologyGateway);
    }

    @Test
    void findCapabilitiesByBootcampIds_ShouldReturnEmpty_WhenNoBootcampsFound() {
        // Given
        List<Long> bootcampIds = List.of(999L);
        String messageId = "msg-789";

        when(capabilityPersistencePort.findCapabilitiesByBootcampIds(bootcampIds))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.findCapabilitiesByBootcampIds(bootcampIds, messageId))
                .verifyComplete();

        verify(capabilityPersistencePort).findCapabilitiesByBootcampIds(bootcampIds);
        verifyNoInteractions(technologyGateway);
    }

    @Test
    void findCapabilitiesByBootcampIds_ShouldReturnBootcampsDirectly_WhenCapabilityIdsAreEmpty() {
        // Given
        List<Long> bootcampIds = List.of(100L);
        String messageId = "msg-888";

        CapabilityBootcamp bootcampWithoutCapabilities = new CapabilityBootcamp(100L, Collections.emptyList());

        when(capabilityPersistencePort.findCapabilitiesByBootcampIds(bootcampIds))
                .thenReturn(Flux.just(bootcampWithoutCapabilities));

        // When & Then
        StepVerifier.create(useCase.findCapabilitiesByBootcampIds(bootcampIds, messageId))
                .assertNext(result -> {
                    assertThat(result.bootcampId()).isEqualTo(100L);
                    assertThat(result.capabilities()).isEmpty();
                })
                .verifyComplete();

        verify(capabilityPersistencePort).findCapabilitiesByBootcampIds(bootcampIds);
        verifyNoInteractions(technologyGateway);
        verifyNoInteractions(technologyMapper);
    }
}
