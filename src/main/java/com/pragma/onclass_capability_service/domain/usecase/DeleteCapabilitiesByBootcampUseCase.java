package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.api.DeleteCapabilitiesByBootcampServicePort;
import com.pragma.onclass_capability_service.domain.spi.BootcampCapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteCapabilitiesByBootcampUseCase implements DeleteCapabilitiesByBootcampServicePort {

    private final BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort;
    private final CapabilityPersistencePort capabilityPersistencePort;
    private final TechnologyGateway technologyGateway;

    @Override
    public Mono<Void> deleteCapabilitiesByBootcamp(Long bootcampId, String messageId) {

        // Retrieve the capabilities associated with this bootcamp from our database
        return bootcampCapabilityPersistencePort.findCapabilitiesByBootcampId(bootcampId)
                .collectList()
                .flatMap(capabilityIds -> {
                    if (capabilityIds.isEmpty()) {
                        return Mono.empty();
                    }

                    return bootcampCapabilityPersistencePort.deleteByBootcampId(bootcampId)
                            .then(
                                    Flux.fromIterable(capabilityIds)
                                            .flatMap(capabilityId ->
                                                    // Check how many bootcamps currently use this capability
                                                    bootcampCapabilityPersistencePort.countBootcampsByCapabilityId(capabilityId)
                                                            .filter(count -> count == 0) // If the count is 0 (no other bootcamp uses it)
                                                            .flatMap(count ->
                                                                    // Eliminate the capability and its associated technologies
                                                                    capabilityPersistencePort.deleteById(capabilityId)
                                                                            .then(technologyGateway.deleteTechnologiesByCapability(capabilityId, messageId))
                                                            )
                                            )
                                            .then()
                            );
                });
    }
}
