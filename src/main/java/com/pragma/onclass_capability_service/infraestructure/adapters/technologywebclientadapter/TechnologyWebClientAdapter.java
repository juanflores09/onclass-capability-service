package com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter;

import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.domain.exceptions.TechnicalException;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.domain.spi.TechnologyGateway;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.dto.TechnologyIdsRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.adapters.technologywebclientadapter.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
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
    public Mono<Boolean> validateTechnologiesExist(List<Long> ids, String messageId) {

        TechnologyIdsRequestDTO requestBody = new TechnologyIdsRequestDTO(ids);

        return webClient.post()
                .uri("/api/v1/technologies/validate")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(Constants.X_MESSAGE_ID, messageId)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> buildErrorResponse(response, TechnicalMessage.INVALID_PARAMETERS))
                .onStatus(HttpStatusCode::is5xxServerError, response -> buildErrorResponse(response, TechnicalMessage.INTERNAL_ERROR_IN_ADAPTERS))
                .bodyToMono(Boolean.class)
                .doOnNext(response -> log.info("Received API response for messageId {}: {}", messageId, response))
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<Technology> getTechnologiesByIds(List<Long> ids) {

        TechnologyIdsRequestDTO requestBody = new TechnologyIdsRequestDTO(ids);

        return webClient.post()
                .uri("/api/v1/technologies/find")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(Constants.X_MESSAGE_ID, "")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> buildErrorResponse(response, TechnicalMessage.INVALID_PARAMETERS))
                .onStatus(HttpStatusCode::is5xxServerError, response -> buildErrorResponse(response, TechnicalMessage.INTERNAL_ERROR_IN_ADAPTERS))
                .bodyToFlux(Technology.class);
    }

    private Mono<Throwable> buildErrorResponse(ClientResponse response, TechnicalMessage technicalMessage) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty(Constants.NO_ADITIONAL_ERROR_DETAILS)
                .flatMap(errorBody -> {
                    log.error(Constants.STRING_ERROR_BODY_DATA, errorBody);
                    return Mono.error(
                            response.statusCode().is5xxServerError() ?
                                    new TechnicalException(technicalMessage):
                                    new BusinessException(technicalMessage));
                });
    }
}
