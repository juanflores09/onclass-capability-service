package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityResponseDTO;
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
    public Flux<CapabilityResponseDTO> listCapabilities(int page, int size, String sortBy, String direction) {
        long offset = (long) page * size;

        return capabilityPersistencePort.findAllPagedAndSorted(sortBy, direction, size, offset)
                .flatMap(capabilityDto ->
                    capabilityPersistencePort.findByCapabilityId(capabilityDto.id())
                            .collectList()
                            .flatMap(techIds ->{
                                if(techIds.isEmpty()){
                                    return Mono.just(new CapabilityResponseDTO(
                                            capabilityDto.id(),
                                            capabilityDto.name(),
                                            capabilityDto.description(),
                                            Collections.emptyList()
                                    ));
                                }

                                // Call the microservice technology-service with the IDs to retrieve ID and Name
                                return technologyGateway.getTechnologiesByIds(techIds)
                                        .collectList()
                                        .map( techList -> new CapabilityResponseDTO(
                                                capabilityDto.id(),
                                                capabilityDto.name(),
                                                capabilityDto.description(),
                                                techList
                                        ));
                            })

                );

    }

}
