package com.pragma.onclass_capability_service.domain.spi;

import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.TechnologyResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyGateway {

    Mono<Boolean> validateTechnologiesExist(List<Long> ids, String messageId);

    Flux<TechnologyResponseDTO> getTechnologiesByIds(List<Long> technologyIds);
}
