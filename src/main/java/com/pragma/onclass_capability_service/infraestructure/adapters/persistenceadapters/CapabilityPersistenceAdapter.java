package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityTechnologyEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataCapabilityTechnologyRepository;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataCapabilityRepository;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CapabilityPersistenceAdapter implements CapabilityPersistencePort {

    private final SpringDataCapabilityRepository repository;
    private final SpringDataCapabilityTechnologyRepository capabilityTechnologyRepository;
    private final CapabilityMapper capabilityMapper;

    @Override
    public Mono<Capability> save(Capability capability) {

        CapabilityEntity entity = new CapabilityEntity();
        entity.setName(capability.getName());
        entity.setDescription(capability.getDescription());

        return repository.save(entity)
                .flatMap(saved -> {
                    List<Long> techIds = capability.getTechnologies() == null? Collections.emptyList():
                            capability.getTechnologies().stream()
                            .map(Technology::getId)
                            .toList();

                    if (techIds.isEmpty()) {
                        return Mono.just(saved);
                    }

                    // Insert the relationships in the intermediate table for each technology.
                    return Flux.fromIterable(techIds)
                            .flatMap(techId -> {
                                CapabilityTechnologyEntity capabilityTechnology = new CapabilityTechnologyEntity();
                                capabilityTechnology.setCapabilityId(saved.getId());
                                capabilityTechnology.setTechnologyId(techId);
                                return capabilityTechnologyRepository.save(capabilityTechnology);
                            }, 5)
                            .then(Mono.just(saved));
                })
                .map(saved -> new Capability(
                        saved.getId(),
                        saved.getName(),
                        saved.getDescription(),
                        capability.getTechnologies()
                ));
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Flux<Capability> findAllPagedAndSorted(String sortBy, String direction, int limit, long offset) {
        return repository.findAllPagedAndSorted(sortBy, direction, limit, offset)
                .map(capabilityMapper::entityToDomain);
    }

    @Override
    public Flux<Long> findByCapabilityId(Long capabilityId) {
        return capabilityTechnologyRepository.findByCapabilityId(capabilityId);
    }

    @Override
    public Mono<Boolean> areAllCapabilitiesExist(List<Long> ids) {
        return repository.countByIdIn(ids)
                .map(count -> count == ids.size());
    }

    @Override
    public Flux<Capability> findByIdIn(List<Long> ids) {
        return repository.findByIdIn(ids)
                .map(capabilityMapper::entityToDomain);
    }
}
