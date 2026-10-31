package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters;

import com.pragma.onclass_capability_service.domain.spi.BootcampCapabilityPersistencePort;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityBootcampEntity;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository.SpringDataBootcampCapabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BootcampCapabilityPersistenceAdapter implements BootcampCapabilityPersistencePort {

    private final SpringDataBootcampCapabilityRepository capabilityBootcampRepository;

    @Override
    public Mono<Void> saveAll(Long bootcampId, List<Long> capabilityIds) {
        return Flux.fromIterable(capabilityIds)
                .map(capabilityId -> {
                    CapabilityBootcampEntity entity = new CapabilityBootcampEntity();
                    entity.setBootcampId(bootcampId);
                    entity.setCapabilityId(capabilityId);
                    return entity;
                })
                .flatMap(capabilityBootcampRepository::save, 5)
                .then();
    }

    @Override
    public Flux<Long> findCapabilitiesByBootcampId(Long bootcampId) {
        return capabilityBootcampRepository.findByBootcampId(bootcampId)
                .map(CapabilityBootcampEntity::getCapabilityId);
    }

    @Override
    public Mono<Void> deleteByBootcampId(Long bootcampId) {
        return capabilityBootcampRepository.deleteByBootcampId(bootcampId);
    }

    @Override
    public Mono<Long> countBootcampsByCapabilityId(Long capabilityId) {
        return capabilityBootcampRepository.countBootcampsByCapabilityId(capabilityId);
    }
}
