package com.pragma.onclass_capability_service.domain.api;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import com.pragma.onclass_capability_service.domain.model.PageResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FindCapabilitiesServicePort {

    Mono<Boolean> areAllCapabilitiesExist(List<Long> ids);
    Mono<PageResult<Capability>> listCapabilities(int page, int size, String sortBy, String direction, String message);
    Flux<CapabilityBootcamp> findCapabilitiesByBootcampIds(List<Long> ids, String messageId);
}
