package com.pragma.onclass_capability_service.domain.spi;

import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityIdsRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityTechnologiesResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyGateway {

    Flux<CapabilityTechnologiesResponseDTO> getTechnologiesByCapabilityId(CapabilityIdsRequestDTO capabilityId, String messageId);
    Mono<Void> saveCapabilityTechnologies(Long capabilityId, List<Long> technologyIds, String messageId);
    Mono<Void> deleteTechnologiesByCapability(Long capabilityId, String messageId);
}
