package com.pragma.onclass_capability_service.domain.spi;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampCapabilityPersistencePort {

    Mono<Void> saveAll(Long capabilityId, List<Long> technologyIds);

    Flux<Long> findCapabilitiesByBootcampId(Long bootcampId);

    Mono<Void> deleteByBootcampId(Long bootcampId);

    Mono<Long> countBootcampsByCapabilityId(Long capabilityId);

}
