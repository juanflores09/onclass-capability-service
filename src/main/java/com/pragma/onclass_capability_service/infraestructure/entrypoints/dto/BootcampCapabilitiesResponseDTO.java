package com.pragma.onclass_capability_service.infraestructure.entrypoints.dto;

import java.util.List;

public record BootcampCapabilitiesResponseDTO (
    Long bootcampId,
    List<CapabilityResponseDTO> capabilities
){
}
