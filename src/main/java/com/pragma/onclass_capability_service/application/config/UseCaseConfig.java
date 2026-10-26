package com.pragma.onclass_capability_service.application.config;

import com.pragma.onclass_capability_service.domain.api.CapabilityServicePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyValidatorGateway;
import com.pragma.onclass_capability_service.domain.usecase.CapabilityUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CapabilityServicePort technologyServicePort(
            CapabilityPersistencePort capabilityPersistencePort,
            TechnologyValidatorGateway technologyValidatorGateway){
        return new CapabilityUseCase(capabilityPersistencePort, technologyValidatorGateway);
    }
}
