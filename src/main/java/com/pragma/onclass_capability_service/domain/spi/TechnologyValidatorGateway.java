package com.pragma.onclass_capability_service.domain.spi;

import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyValidatorGateway {

    Mono<Boolean> validateTechnologiesExist(List<Long> ids, String messageId);
}
