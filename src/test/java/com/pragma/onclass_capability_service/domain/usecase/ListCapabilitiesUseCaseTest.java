package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.PageResult;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListCapabilitiesUseCaseTest {

    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private TechnologyGateway technologyGateway;

    @InjectMocks
    private ListCapabilitiesUseCase listCapabilitiesUseCase;

    private Capability capability;
    private Technology technology;

    @BeforeEach
    void setUp() {
        technology = new Technology(1L, "Spring Boot", "Framework");

        capability = new Capability();
        capability.setId(10L);
        capability.setName("Backend Development");
        capability.setDescription("Desarrollo backend");
    }

    @Test
    void areAllCapabilitiesExist_ReturnsTrue() {
        List<Long> ids = List.of(10L, 20L);
        when(capabilityPersistencePort.areAllCapabilitiesExist(ids)).thenReturn(Mono.just(true));

        Mono<Boolean> result = listCapabilitiesUseCase.areAllCapabilitiesExist(ids);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).areAllCapabilitiesExist(ids);
    }

    @Test
    void listCapabilities_Success() {
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";
        long offset = 0L;

        when(capabilityPersistencePort.findAllPagedAndSorted(sortBy, direction, size, offset))
                .thenReturn(Flux.just(capability));
        when(capabilityPersistencePort.findByCapabilityId(capability.getId()))
                .thenReturn(Flux.just(1L));
        when(technologyGateway.getTechnologiesByIds(List.of(1L)))
                .thenReturn(Flux.just(technology));
        when(capabilityPersistencePort.countTotal())
                .thenReturn(Mono.just(1L));

        Mono<PageResult<Capability>> result = listCapabilitiesUseCase.listCapabilities(page, size, sortBy, direction);

        StepVerifier.create(result)
                .expectNextMatches(pageResult ->
                        pageResult.totalElements() == 1L &&
                                pageResult.content().size() == 1 &&
                                pageResult.content().getFirst().getTechnologies().size() == 1 &&
                                pageResult.content().getFirst().getTechnologies().getFirst().getName().equals("Spring Boot")
                )
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).findAllPagedAndSorted(sortBy, direction, size, offset);
        verify(capabilityPersistencePort, times(1)).findByCapabilityId(capability.getId());
        verify(technologyGateway, times(1)).getTechnologiesByIds(List.of(1L));
        verify(capabilityPersistencePort, times(1)).countTotal();
    }

    @Test
    void listCapabilities_WithoutTechnologies_Success() {
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "ASC";
        long offset = 0L;

        when(capabilityPersistencePort.findAllPagedAndSorted(sortBy, direction, size, offset))
                .thenReturn(Flux.just(capability));
        when(capabilityPersistencePort.findByCapabilityId(capability.getId()))
                .thenReturn(Flux.empty());
        when(capabilityPersistencePort.countTotal())
                .thenReturn(Mono.just(1L));

        Mono<PageResult<Capability>> result = listCapabilitiesUseCase.listCapabilities(page, size, sortBy, direction);

        StepVerifier.create(result)
                .expectNextMatches(pageResult ->
                        pageResult.totalElements() == 1L &&
                                pageResult.content().getFirst().getTechnologies().isEmpty()
                )
                .verifyComplete();

        verify(technologyGateway, never()).getTechnologiesByIds(any());
    }

    @Test
    void findByIdIn_Success() {
        List<Long> ids = List.of(10L);

        when(capabilityPersistencePort.findByIdIn(ids)).thenReturn(Flux.just(capability));
        when(capabilityPersistencePort.findByCapabilityId(capability.getId()))
                .thenReturn(Flux.just(1L));
        when(technologyGateway.getTechnologiesByIds(List.of(1L)))
                .thenReturn(Flux.just(technology));

        Flux<Capability> result = listCapabilitiesUseCase.findByIdIn(ids);

        StepVerifier.create(result)
                .expectNextMatches(cap -> cap.getTechnologies().size() == 1)
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).findByIdIn(ids);
        verify(technologyGateway, times(1)).getTechnologiesByIds(List.of(1L));
    }
}
