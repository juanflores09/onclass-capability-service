package com.pragma.onclass_capability_service.domain.spi;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityPersistencePort {

    Mono<Capability> save(Capability capability);
    Mono<Boolean> existsByName(String name);
    Flux<Capability> findAllPagedAndSorted(String sortBy, String direction, int limit, long offset);
    Mono<Boolean> areAllCapabilitiesExist(List<Long> ids);
    Flux<CapabilityBootcamp> findCapabilitiesByBootcampIds(List<Long> ids);
    Mono<Long> countTotal();
    Mono<Void> deleteById(Long id);
    Mono<Integer> sumTechnologiesByBootcampId(Long bootcampId);
}
