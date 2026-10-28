package com.pragma.onclass_capability_service.infraestructure.adapters;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.CapabilityPersistenceAdapter;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityTechnologyEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataCapabilityRepository;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataCapabilityTechnologyRepository;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CapabilityPersistenceAdapterTest {

    @Mock
    private SpringDataCapabilityRepository repository;

    @Mock
    private SpringDataCapabilityTechnologyRepository capabilityTechnologyRepository;

    @Mock
    private CapabilityMapper capabilityMapper;

    @InjectMocks
    private CapabilityPersistenceAdapter persistenceAdapter;

    private Capability capability;
    private CapabilityEntity capabilityEntity;

    @BeforeEach
    void setUp() {
        Technology technology = new Technology(1L, "Spring Boot", "Framework");

        capability = new Capability();
        capability.setId(1L);
        capability.setName("Backend");
        capability.setDescription("Backend Development");
        capability.setTechnologies(List.of(technology));

        capabilityEntity = new CapabilityEntity();
        capabilityEntity.setId(1L);
        capabilityEntity.setName("Backend");
        capabilityEntity.setDescription("Backend Development");
    }

    @Test
    void save_WithTechnologies_Success() {
        when(capabilityMapper.capabilityToCapabilityEntity(capability)).thenReturn(capabilityEntity);
        when(repository.save(capabilityEntity)).thenReturn(Mono.just(capabilityEntity));
        when(capabilityTechnologyRepository.save(any(CapabilityTechnologyEntity.class)))
                .thenReturn(Mono.just(new CapabilityTechnologyEntity()));
        when(capabilityMapper.capabilityEntityToCapability(capabilityEntity, capability.getTechnologies()))
                .thenReturn(capability);

        Mono<Capability> result = persistenceAdapter.save(capability);

        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(repository, times(1)).save(capabilityEntity);
        verify(capabilityTechnologyRepository, times(1)).save(any(CapabilityTechnologyEntity.class));
        verify(capabilityMapper, times(1)).capabilityEntityToCapability(capabilityEntity, capability.getTechnologies());
    }

    @Test
    void save_WithoutTechnologies_Success() {
        capability.setTechnologies(Collections.emptyList());
        when(capabilityMapper.capabilityToCapabilityEntity(capability)).thenReturn(capabilityEntity);
        when(repository.save(capabilityEntity)).thenReturn(Mono.just(capabilityEntity));
        when(capabilityMapper.capabilityEntityToCapability(capabilityEntity, capability.getTechnologies()))
                .thenReturn(capability);

        Mono<Capability> result = persistenceAdapter.save(capability);

        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(repository, times(1)).save(capabilityEntity);
        verify(capabilityTechnologyRepository, never()).save(any());
    }

    @Test
    void existsByName_ReturnsTrue() {
        when(repository.existsByName("Backend")).thenReturn(Mono.just(true));

        Mono<Boolean> result = persistenceAdapter.existsByName("Backend");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(repository, times(1)).existsByName("Backend");
    }

    @Test
    void findAllPagedAndSorted_Success() {
        String sortBy = "name";
        String direction = "ASC";
        int limit = 10;
        long offset = 0L;

        when(repository.findAllPagedAndSorted(sortBy, direction, limit, offset))
                .thenReturn(Flux.just(capabilityEntity));
        when(capabilityMapper.capabilityEntityToCapability(capabilityEntity))
                .thenReturn(capability);

        Flux<Capability> result = persistenceAdapter.findAllPagedAndSorted(sortBy, direction, limit, offset);

        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(repository, times(1)).findAllPagedAndSorted(sortBy, direction, limit, offset);
    }

    @Test
    void findByCapabilityId_Success() {
        when(capabilityTechnologyRepository.findByCapabilityId(1L)).thenReturn(Flux.just(1L, 2L));

        Flux<Long> result = persistenceAdapter.findByCapabilityId(1L);

        StepVerifier.create(result)
                .expectNext(1L, 2L)
                .verifyComplete();

        verify(capabilityTechnologyRepository, times(1)).findByCapabilityId(1L);
    }

    @Test
    void areAllCapabilitiesExist_ReturnsTrue() {
        List<Long> ids = List.of(1L, 2L);
        when(repository.countByIdIn(ids)).thenReturn(Mono.just(2L));

        Mono<Boolean> result = persistenceAdapter.areAllCapabilitiesExist(ids);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(repository, times(1)).countByIdIn(ids);
    }

    @Test
    void findByIdIn_Success() {
        List<Long> ids = List.of(1L);
        when(repository.findByIdIn(ids)).thenReturn(Flux.just(capabilityEntity));
        when(capabilityMapper.capabilityEntityToCapability(capabilityEntity)).thenReturn(capability);

        Flux<Capability> result = persistenceAdapter.findByIdIn(ids);

        StepVerifier.create(result)
                .expectNext(capability)
                .verifyComplete();

        verify(repository, times(1)).findByIdIn(ids);
    }

    @Test
    void countTotal_Success() {
        when(repository.count()).thenReturn(Mono.just(5L));

        Mono<Long> result = persistenceAdapter.countTotal();

        StepVerifier.create(result)
                .expectNext(5L)
                .verifyComplete();

        verify(repository, times(1)).count();
    }
}
