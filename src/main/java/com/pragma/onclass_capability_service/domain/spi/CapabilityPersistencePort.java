package com.pragma.onclass_capability_service.domain.spi;

import com.pragma.onclass_capability_service.domain.model.Capability;
import reactor.core.publisher.Mono;

public interface CapabilityPersistencePort {

    Mono<Capability> save(Capability capability);
    Mono<Boolean> existsByName(String name);
}
