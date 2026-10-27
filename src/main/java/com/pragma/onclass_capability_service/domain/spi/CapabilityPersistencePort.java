package com.pragma.onclass_capability_service.domain.spi;

import com.pragma.onclass_capability_service.domain.model.Capability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityPersistencePort {

    Mono<Capability> save(Capability capability);
    Mono<Boolean> existsByName(String name);
    Flux<Capability> findAllPagedAndSorted(String sortBy, String direction, int limit, long offset);

    Flux<Long> findByCapabilityId(Long capabilityId);
    Mono<Boolean> areAllCapabilitiesExist(List<Long> ids);
    Flux<Capability> findByIdIn(List<Long> ids);
}
