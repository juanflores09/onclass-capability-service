package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.api.CreateCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateCapabilityUseCase implements CreateCapabilityServicePort {

    private final CapabilityPersistencePort capabilityPersistencePort;
    private final TechnologyGateway technologyValidatorGateway;

    @Override
    @Transactional
    public Mono<Capability> createTechnology(Capability capability, String messageId) {

        return capabilityPersistencePort.existsByName(capability.getName())
                .filter(exits -> !exits)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_ALREADY_EXISTS)))
                .flatMap(ignored -> technologyValidatorGateway.validateTechnologiesExist(capability.getTechnologies(), messageId))
                .filter(Boolean.TRUE::equals)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_FOUND)))
                .flatMap(ignored -> capabilityPersistencePort.save(capability));
    }
}
