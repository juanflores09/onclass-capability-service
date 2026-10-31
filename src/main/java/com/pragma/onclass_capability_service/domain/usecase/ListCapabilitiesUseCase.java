package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import com.pragma.onclass_capability_service.domain.model.PageResult;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityIdsRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityTechnologiesResponseDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.TechnologyDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.TechnologyMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ListCapabilitiesUseCase implements FindCapabilitiesServicePort {

    private final CapabilityPersistencePort capabilityPersistencePort;
    private final TechnologyGateway technologyGateway;
    private final TechnologyMapper technologyMapper;

    @Override
    public Mono<Boolean> areAllCapabilitiesExist(List<Long> ids) {
        return capabilityPersistencePort.areAllCapabilitiesExist(ids);
    }

    @Override
    public Mono<PageResult<Capability>> listCapabilities(int page, int size, String sortBy, String direction, String message) {
        long offset = (long) page * size;

        Flux<Capability> capabilityFlux = capabilityPersistencePort.findAllPagedAndSorted(sortBy, direction, size, offset)
                .collectList()
                .flatMapMany(capabilities -> {

                    if (capabilities.isEmpty()) {
                        return Flux.empty();
                    }

                    List<Long> capabilityIds = capabilities.stream()
                            .map(Capability::getId)
                            .toList();
                    CapabilityIdsRequestDTO capabilityIdsRequestDTO = new CapabilityIdsRequestDTO(capabilityIds);

                    return technologyGateway.getTechnologiesByCapabilityId(capabilityIdsRequestDTO, message)
                            .collectMap(
                                    CapabilityTechnologiesResponseDTO::capabilityId,
                                    CapabilityTechnologiesResponseDTO::technologies
                            )
                            .flatMapMany(techMap ->
                                Flux.fromIterable(capabilities)
                                    .map(capability -> {

                                        List<TechnologyDTO> techDTOs = techMap.getOrDefault(capability.getId(), Collections.emptyList());

                                        List<Technology> domainTechnologies = techDTOs.stream()
                                                .map(dto -> new Technology(
                                                        dto.id(),
                                                        dto.name(),
                                                        dto.description()
                                                ))
                                                .toList();

                                        capability.setTechnologies(domainTechnologies);
                                        return capability;
                                    })

                            );
                });

        // Gets the total number of items in the database
        Mono<Long> totalCountMono = capabilityPersistencePort.countTotal();

        // Combine both results into a domain PageResult.
        return Mono.zip(capabilityFlux.collectList(), totalCountMono)
                .map(tuple -> new PageResult<>(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Flux<CapabilityBootcamp> findCapabilitiesByBootcampIds(List<Long> ids, String messageId) {
        return capabilityPersistencePort.findCapabilitiesByBootcampIds(ids)
                .collectList()
                .flatMapMany(capabilityBootcamp ->{

                    if (capabilityBootcamp.isEmpty()) {
                        return Flux.empty();
                    }

                    List<Long> allCapabilityIds = capabilityBootcamp.stream()
                            .flatMap(cb -> cb.capabilities().stream())
                            .map(Capability::getId)
                            .distinct()
                            .toList();

                    if (allCapabilityIds.isEmpty()) {
                        return Flux.fromIterable(capabilityBootcamp);
                    }

                    CapabilityIdsRequestDTO capabilityIdsRequestDTO = new CapabilityIdsRequestDTO(allCapabilityIds);

                    return technologyGateway.getTechnologiesByCapabilityId(capabilityIdsRequestDTO, messageId)
                            .collectMap(
                                    CapabilityTechnologiesResponseDTO::capabilityId,
                                    CapabilityTechnologiesResponseDTO::technologies
                            )
                            .map(techMap ->

                                capabilityBootcamp.stream()
                                        .map(cb -> new CapabilityBootcamp(
                                                cb.bootcampId(),
                                                cb.capabilities().stream()
                                                        .map(capability -> {

                                                            List<TechnologyDTO> techDTOs = techMap
                                                                    .getOrDefault(capability.getId(), Collections.emptyList());

                                                            return new Capability(
                                                                    capability.getId(),
                                                                    capability.getName(),
                                                                    capability.getDescription(),
                                                                    techDTOs.stream()
                                                                            .map(technologyMapper::technologyDtoToTechnology)
                                                                            .toList()
                                                            );
                                                        })
                                                        .toList()
                                        ))
                                        .toList()

                            )
                            .flatMapMany(Flux::fromIterable);
                        }
                );

    }

}
