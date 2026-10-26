package com.pragma.onclass_capability_service.domain.api;

import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityResponseDTO;
import reactor.core.publisher.Flux;

public interface FindCapabilitiesServicePort {

    Flux<CapabilityResponseDTO> listCapabilities(int page, int size, String sortBy, String direction);
}
