package com.pragma.onclass_capability_service.application.config;

import com.pragma.onclass_capability_service.domain.api.CreateCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.domain.usecase.CreateCapabilityUseCase;
import com.pragma.onclass_capability_service.domain.usecase.ListCapabilitiesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateCapabilityServicePort technologyServicePort(
            CapabilityPersistencePort capabilityPersistencePort,
            TechnologyGateway technologyValidatorGateway){
        return new CreateCapabilityUseCase(capabilityPersistencePort, technologyValidatorGateway);
    }

    @Bean
    public FindCapabilitiesServicePort findCapabilitiesServicePort(
            CapabilityPersistencePort capabilityPersistencePort,
            TechnologyGateway technologyValidatorGateway){
        return new ListCapabilitiesUseCase(capabilityPersistencePort, technologyValidatorGateway);
    }
}
