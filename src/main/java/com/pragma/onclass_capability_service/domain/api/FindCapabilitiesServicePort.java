package com.pragma.onclass_capability_service.domain.api;

import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FindCapabilitiesServicePort {

    Mono<Boolean> areAllCapabilitiesExist(List<Long> ids);
    Flux<CapabilityResponseDTO> listCapabilities(int page, int size, String sortBy, String direction);
}
