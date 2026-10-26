package com.pragma.onclass_capability_service.domain.api;

import com.pragma.onclass_capability_service.domain.model.Capability;
import reactor.core.publisher.Mono;

public interface CreateCapabilityServicePort {

    Mono<Capability> createCapability(Capability capability, String messageId);
}
