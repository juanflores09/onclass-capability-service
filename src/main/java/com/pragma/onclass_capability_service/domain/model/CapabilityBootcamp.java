package com.pragma.onclass_capability_service.domain.model;

import java.util.List;

public record CapabilityBootcamp (
        Long bootcampId,
        List<Capability> capabilities
){
}
