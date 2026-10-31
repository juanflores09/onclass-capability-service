package com.pragma.onclass_capability_service.infraestructure.adapters;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.CapabilityPersistenceAdapter;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.BootcampCapabilityProjection;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataCapabilityRepository;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CapabilityPersistenceAdapterTest {

    @Mock
    private SpringDataCapabilityRepository repository;

    @Mock
    private CapabilityMapper capabilityMapper;

    @InjectMocks
    private CapabilityPersistenceAdapter adapter;

    @Test
    void save_ShouldReturnSavedCapability() {
        // Given
        Capability capability = new Capability(1L, "Java", "Backend language");
        CapabilityEntity entity = new CapabilityEntity();
        entity.setId(1L);
        entity.setName("Java");
        entity.setDescription("Backend language");
        entity.setTechnologyCount(0);

        when(capabilityMapper.capabilityToCapabilityEntity(capability)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(capabilityMapper.capabilityEntityToCapability(eq(entity), any())).thenReturn(capability);

        // When & Then
        StepVerifier.create(adapter.save(capability))
                .expectNext(capability)
                .verifyComplete();

        verify(capabilityMapper).capabilityToCapabilityEntity(capability);
        verify(repository).save(entity);
        verify(capabilityMapper).capabilityEntityToCapability(eq(entity), any());
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenNameExists() {
        // Given
        String name = "Java";
        when(repository.existsByName(name)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(adapter.existsByName(name))
                .expectNext(true)
                .verifyComplete();

        verify(repository).existsByName(name);
    }

    @Test
    void findAllPagedAndSorted_ShouldReturnPagedCapabilities() {
        // Given
        String sortBy = "name";
        String direction = "ASC";
        int limit = 10;
        long offset = 0L;

        CapabilityEntity entity = new CapabilityEntity();
        entity.setId(1L);
        entity.setName("Java");
        entity.setDescription("Backend language");
        entity.setTechnologyCount(0);

        Capability capability = new Capability(1L, "Java", "Backend");

        when(repository.findAllPagedAndSorted(sortBy, direction, limit, offset)).thenReturn(Flux.just(entity));
        when(capabilityMapper.capabilityEntityToCapability(entity)).thenReturn(capability);

        // When & Then
        StepVerifier.create(adapter.findAllPagedAndSorted(sortBy, direction, limit, offset))
                .expectNext(capability)
                .verifyComplete();

        verify(repository).findAllPagedAndSorted(sortBy, direction, limit, offset);
        verify(capabilityMapper).capabilityEntityToCapability(entity);
    }

    @Test
    void areAllCapabilitiesExist_ShouldReturnTrue_WhenAllIdsExist() {
        // Given
        List<Long> ids = List.of(1L, 2L);
        when(repository.countByIdIn(ids)).thenReturn(Mono.just(2L));

        // When & Then
        StepVerifier.create(adapter.areAllCapabilitiesExist(ids))
                .expectNext(true)
                .verifyComplete();

        verify(repository).countByIdIn(ids);
    }

    @Test
    void areAllCapabilitiesExist_ShouldReturnFalse_WhenSomeIdsDoNotExist() {
        // Given
        List<Long> ids = List.of(1L, 2L, 3L);
        when(repository.countByIdIn(ids)).thenReturn(Mono.just(2L)); // Faltan registros

        // When & Then
        StepVerifier.create(adapter.areAllCapabilitiesExist(ids))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findCapabilitiesByBootcampIds_ShouldReturnGroupedCapabilities() {
        // Given
        List<Long> bootcampIds = List.of(100L);
        BootcampCapabilityProjection projection = new BootcampCapabilityProjection(100L, 1L, "Java", "Backend");

        when(repository.findCapabilitiesByBootcampId(bootcampIds)).thenReturn(Flux.just(projection));

        // When & Then
        StepVerifier.create(adapter.findCapabilitiesByBootcampIds(bootcampIds))
                .assertNext(capabilityBootcamp -> {
                    assert capabilityBootcamp.bootcampId().equals(100L);
                    assert capabilityBootcamp.capabilities().size() == 1;
                    assert capabilityBootcamp.capabilities().get(0).getName().equals("Java");
                })
                .verifyComplete();

        verify(repository).findCapabilitiesByBootcampId(bootcampIds);
    }

    @Test
    void countTotal_ShouldReturnTotalCount() {
        // Given
        when(repository.count()).thenReturn(Mono.just(50L));

        // When & Then
        StepVerifier.create(adapter.countTotal())
                .expectNext(50L)
                .verifyComplete();

        verify(repository).count();
    }

    @Test
    void deleteById_ShouldCompleteSuccessfully() {
        // Given
        Long id = 1L;
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.deleteById(id))
                .verifyComplete();

        verify(repository).deleteById(id);
    }
}
