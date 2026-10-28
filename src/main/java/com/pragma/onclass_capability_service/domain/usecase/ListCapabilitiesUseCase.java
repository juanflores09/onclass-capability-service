package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.PageResult;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ListCapabilitiesUseCase implements FindCapabilitiesServicePort {

    private final CapabilityPersistencePort capabilityPersistencePort;
    private final TechnologyGateway technologyGateway;

    @Override
    public Mono<Boolean> areAllCapabilitiesExist(List<Long> ids) {
        return capabilityPersistencePort.areAllCapabilitiesExist(ids);
    }

    @Override
    public Mono<PageResult<Capability>> listCapabilities(int page, int size, String sortBy, String direction) {
        long offset = (long) page * size;

        Flux<Capability> capabilityFlux = capabilityPersistencePort.findAllPagedAndSorted(sortBy, direction, size, offset)
                .flatMapSequential(capability ->
                    capabilityPersistencePort.findByCapabilityId(capability.getId())
                            .collectList()
                            .flatMap(techIds ->{
                                if(techIds.isEmpty()){
                                    capability.setTechnologies(Collections.emptyList());
                                    return Mono.just(capability);
                                }

                                // Call the microservice technology-service with the IDs to retrieve ID and Name
                                return technologyGateway.getTechnologiesByIds(techIds)
                                        .collectList()
                                        .map( techList -> {
                                            capability.setTechnologies(techList);
                                            return capability;
                                        });
                            })

                );

        // Gets the total number of items in the database
        Mono<Long> totalCountMono = capabilityPersistencePort.countTotal();

        // Combine both results into a domain PageResult.
        return Mono.zip(capabilityFlux.collectList(), totalCountMono)
                .map(tuple -> new PageResult<>(tuple.getT1(), tuple.getT2()));

    }

    @Override
    public Flux<Capability> findByIdIn(List<Long> ids) {
        return capabilityPersistencePort.findByIdIn(ids)
                .flatMap(capability ->
                        capabilityPersistencePort.findByCapabilityId(capability.getId())
                                .collectList()
                                .flatMap(techIds -> {
                                    if(techIds.isEmpty()){
                                        capability.setTechnologies(Collections.emptyList());
                                        return Mono.just(capability);
                                    }

                                    // Call the microservice technology-service with the IDs to retrieve ID and Name
                                    return technologyGateway.getTechnologiesByIds(techIds)
                                            .collectList()
                                            .map( techList -> {
                                                        capability.setTechnologies(techList);
                                                        return capability;
                                                    }
                                            );
                                }));
    }

}
