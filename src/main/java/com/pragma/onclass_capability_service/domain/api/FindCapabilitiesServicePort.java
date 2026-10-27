package com.pragma.onclass_capability_service.domain.api;

import com.pragma.onclass_capability_service.domain.model.Capability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FindCapabilitiesServicePort {

    Mono<Boolean> areAllCapabilitiesExist(List<Long> ids);
    Flux<Capability> listCapabilities(int page, int size, String sortBy, String direction);
    Flux<Capability> findByIdIn(List<Long> ids);
}
