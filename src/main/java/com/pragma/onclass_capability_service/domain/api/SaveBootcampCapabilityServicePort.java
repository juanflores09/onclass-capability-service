package com.pragma.onclass_capability_service.domain.api;

import reactor.core.publisher.Mono;

import java.util.List;


public interface SaveBootcampCapabilityServicePort {

    Mono<Void> saveAll(Long bootcampId, List<Long> capabilityIds);
}
