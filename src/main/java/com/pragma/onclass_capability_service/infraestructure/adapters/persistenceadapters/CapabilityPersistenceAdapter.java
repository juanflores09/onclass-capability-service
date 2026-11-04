package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.BootcampCapabilityProjection;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataCapabilityRepository;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CapabilityPersistenceAdapter implements CapabilityPersistencePort {

    private final SpringDataCapabilityRepository repository;
    private final CapabilityMapper capabilityMapper;

    @Override
    public Mono<Capability> save(Capability capability) {
        CapabilityEntity entity = capabilityMapper.capabilityToCapabilityEntity(capability);

        return repository.save(entity)
                .map(saved ->
                        capabilityMapper.capabilityEntityToCapability(saved, capability.getTechnologies()));
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Flux<Capability> findAllPagedAndSorted(String sortBy, String direction, int limit, long offset) {
        return repository.findAllPagedAndSorted(sortBy, direction, limit, offset)
                .map(capabilityMapper::capabilityEntityToCapability);
    }

    @Override
    public Mono<Boolean> areAllCapabilitiesExist(List<Long> ids) {
        return repository.countByIdIn(ids)
                .map(count -> count == ids.size());
    }

    @Override
    public Flux<CapabilityBootcamp> findCapabilitiesByBootcampIds(List<Long> ids) {
        return repository.findCapabilitiesByBootcampId(ids)
                .collectMultimap(
                        BootcampCapabilityProjection::bootcampId,
                        projection -> new Capability(
                                projection.id(),
                                projection.name(),
                                projection.description()
                        )
                )
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()))
                .map(entry ->
                        new CapabilityBootcamp(entry.getKey(), new ArrayList<>(entry.getValue())));
    }

    @Override
    public Mono<Long> countTotal() {
        return repository.count();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Integer> sumTechnologiesByBootcampId(Long bootcampId) {
        return repository.sumTechnologyCountByBootcampId(bootcampId)
                .defaultIfEmpty(0);
    }
}
