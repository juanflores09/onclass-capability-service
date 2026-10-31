package com.pragma.onclass_capability_service.application.config;

import com.pragma.onclass_capability_service.domain.api.CreateCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.api.DeleteCapabilitiesByBootcampServicePort;
import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.api.SaveBootcampCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.spi.BootcampCapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.CapabilityPersistencePort;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.domain.usecase.CreateCapabilityUseCase;
import com.pragma.onclass_capability_service.domain.usecase.DeleteCapabilitiesByBootcampUseCase;
import com.pragma.onclass_capability_service.domain.usecase.ListCapabilitiesUseCase;
import com.pragma.onclass_capability_service.domain.usecase.SaveBootcampCapabilityUseCase;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.TechnologyMapper;
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
            TechnologyGateway technologyValidatorGateway,
            TechnologyMapper technologyMapper){
        return new ListCapabilitiesUseCase(capabilityPersistencePort, technologyValidatorGateway, technologyMapper);
    }

    @Bean
    public SaveBootcampCapabilityServicePort saveCapabilityTechnologyServicePort(
            CapabilityPersistencePort capabilityPersistencePort,
            BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort){
        return new SaveBootcampCapabilityUseCase(capabilityPersistencePort, bootcampCapabilityPersistencePort);
    }

    @Bean
    public DeleteCapabilitiesByBootcampServicePort deleteCapabilitiesByBootcampServicePort(
            BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort,
            CapabilityPersistencePort capabilityPersistencePort,
            TechnologyGateway technologyGateway
    ){
        return new DeleteCapabilitiesByBootcampUseCase(bootcampCapabilityPersistencePort, capabilityPersistencePort, technologyGateway);
    }
}
