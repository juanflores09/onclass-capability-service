package com.pragma.onclass_capability_service.domain.api;

import reactor.core.publisher.Mono;

public interface DeleteCapabilitiesByBootcampServicePort {
    Mono<Void> deleteCapabilitiesByBootcamp(Long bootcampId, String messageId);
}
