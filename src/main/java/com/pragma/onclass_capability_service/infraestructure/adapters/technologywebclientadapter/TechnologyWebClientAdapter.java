package com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter;

import com.pragma.onclass_capability_service.domain.exceptions.ExternalServiceException;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityIdsRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityTechnologiesRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.CapabilityTechnologiesResponseDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.util.Constants;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TechnologyWebClientAdapter implements TechnologyGateway {

    private final WebClient webClient;

    @Override
    public Flux<CapabilityTechnologiesResponseDTO> getTechnologiesByCapabilityId(CapabilityIdsRequestDTO capabilityIds, String messageId) {
        return webClient.post()
                .uri("/api/v1/technologies/find-by-capabilities")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(Constants.X_MESSAGE_ID, messageId)
                .bodyValue(capabilityIds)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiResponseDTO.class)
                                .flatMap(errorResponse -> Mono.error(new ExternalServiceException(errorResponse)))
                )
                .bodyToFlux(CapabilityTechnologiesResponseDTO.class);
    }

    @Override
    public Mono<Void> saveCapabilityTechnologies(Long capabilityId, List<Long> technologyIds, String messageId) {

        CapabilityTechnologiesRequestDTO requestBody = new CapabilityTechnologiesRequestDTO(capabilityId, technologyIds);

        return webClient.post()
                .uri("/api/v1/capabilities-technologies")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(Constants.X_MESSAGE_ID, messageId)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiResponseDTO.class)
                                .flatMap(errorResponse -> Mono.error(new ExternalServiceException(errorResponse)))
                )
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Void> deleteTechnologiesByCapability(Long capabilityId, String messageId) {
        return webClient.delete()
                .uri("/api/v1/technologies/capability/{id}", capabilityId)
                .header(Constants.X_MESSAGE_ID, messageId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiResponseDTO.class)
                                .flatMap(errorResponse -> Mono.error(new ExternalServiceException(errorResponse)))
                )
                .bodyToMono(Void.class);
    }
}
