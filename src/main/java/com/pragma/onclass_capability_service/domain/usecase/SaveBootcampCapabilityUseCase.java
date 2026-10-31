package com.pragma.onclass_capability_service.domain.usecase;

import com.pragma.onclass_capability_service.domain.api.SaveBootcampCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.domain.spi.BootcampCapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class SaveBootcampCapabilityUseCase implements SaveBootcampCapabilityServicePort {

    private final CapabilityPersistencePort capabilityPersistencePort;
    private final BootcampCapabilityPersistencePort capabilityTechnologyPersistencePort;

    @Override
    public Mono<Void> saveAll(Long bootcampId, List<Long> capabilityIds) {
        if (capabilityIds == null || capabilityIds.isEmpty()) {
            return Mono.empty();
        }

        return capabilityPersistencePort.areAllCapabilitiesExist(capabilityIds)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NOT_FOUND)))
                .flatMap(count ->
                        capabilityTechnologyPersistencePort.saveAll(bootcampId, capabilityIds)
                );
    }

}
