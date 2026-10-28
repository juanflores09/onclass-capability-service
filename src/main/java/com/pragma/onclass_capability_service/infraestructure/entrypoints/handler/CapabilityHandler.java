package com.pragma.onclass_capability_service.infraestructure.entrypoints.handler;

import com.pragma.onclass_capability_service.domain.api.CreateCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.*;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.util.Constants;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.util.ErrorDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapabilityHandler {

    private final CreateCapabilityServicePort capabilityServicePort;
    private final FindCapabilitiesServicePort findCapabilitiesServicePort;
    private final CapabilityMapper capabilityMapper;
    private final Validator validator;

    public Mono<ServerResponse> createCapability(ServerRequest request) {

        String messageId = request.headers().firstHeader(Constants.X_MESSAGE_ID);

        return request.bodyToMono(CapabilityRequestDTO.class)
                .flatMap(dto -> {

                    Set<ConstraintViolation<CapabilityRequestDTO>> violations = validator.validate(dto);

                    if (!violations.isEmpty()) {
                        String errorMessage = violations.iterator().next().getMessage();
                        return Mono.error(new BusinessException(errorMessage, TechnicalMessage.INVALID_PARAMETERS));
                    }

                    Set<Long> uniqueTechnologies = new HashSet<>(dto.technologies());
                    if (uniqueTechnologies.size() < dto.technologies().size()) {
                        return Mono.error(new BusinessException(TechnicalMessage.DUPLICATE_TECHNOLOGIES));
                    }
                    return Mono.just(dto);
                })
                .map(capabilityMapper::capabilityRequestDTOToCapability)
                .flatMap(capability -> capabilityServicePort.createCapability(capability,messageId))
                .map(capabilityMapper::capabilityResponseDTOToCapability)
                .doOnSuccess(saved ->log.info("Capability successfully created  with messageId: {}", messageId))
                .flatMap(saved -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(saved))
                .contextWrite(Context.of(Constants.X_MESSAGE_ID, Objects.requireNonNull(messageId)))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        ex.getTechnicalMessage(),
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getMessage())
                                .build())
                ))
                .onErrorResume(ex -> {
                    log.error("[createTechnology] Unexpected error occurred for messageId: {}", messageId, ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            messageId,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build())
                    );
                });
    }

    public Mono<ServerResponse> listCapabilities(ServerRequest request) {

        String messageId = request.headers().firstHeader(Constants.X_MESSAGE_ID);

        // Get the query params with default values
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);

        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(10);

        String sortBy = request.queryParam("sortBy")
                .orElse("name");

        String direction = request.queryParam("direction")
                .orElse("ASC");

        return findCapabilitiesServicePort.listCapabilities(page, size, sortBy, direction)
                .map(pageResult ->{

                    // Map domain content to response DTOs
                    List<CapabilityResponseDTO> bootcampDTOs = pageResult.content().stream()
                            .map(capability -> capabilityMapper.capabilityResponseDTOToCapability(
                                    capability, capability.getTechnologies()))
                            .toList();

                    // Calculate the total number of pages
                    int totalPages = (int) Math.ceil((double) pageResult.totalElements() / size);

                    // Build the paginated response structure
                    return new PageResponseDTO<>(
                            page+1,
                            size,
                            pageResult.totalElements(),
                            totalPages,
                            bootcampDTOs
                    );
                })
                .flatMap(capabilities -> ServerResponse
                        .status(HttpStatus.OK)
                        .bodyValue(capabilities))
                .contextWrite(Context.of(Constants.X_MESSAGE_ID, Objects.requireNonNullElse(messageId, "")))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        ex.getTechnicalMessage(),
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getMessage())
                                .build())
                ))
                .onErrorResume(ex -> {
                    log.error("[listCapabilities] Unexpected error occurred for messageId: {}", messageId, ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            messageId,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build())
                    );
                });
    }

    public Mono<ServerResponse> validateCapabilities(ServerRequest request) {

        String messageId = request.headers().firstHeader(Constants.X_MESSAGE_ID);

        return request.bodyToMono(CapabilityIdsRequestDTO.class)
                .flatMap(dto -> {

                    Set<ConstraintViolation<CapabilityIdsRequestDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.iterator().next().getMessage();
                        return Mono.error(new BusinessException(errorMessage, TechnicalMessage.INVALID_PARAMETERS));
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> findCapabilitiesServicePort.areAllCapabilitiesExist(dto.ids())
                )
                .flatMap(exists -> ServerResponse
                        .status(HttpStatus.OK)
                        .bodyValue(exists))
                .contextWrite(Context.of(Constants.X_MESSAGE_ID, Objects.requireNonNull(messageId)))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        ex.getTechnicalMessage(),
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getMessage())
                                .build())
                ))
                .onErrorResume(ex -> {
                    log.error("Unexpected error occurred for messageId: {}", messageId, ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            messageId,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build())
                    );
                });
    }

    public Mono<ServerResponse> findCapabilitiesByIds(ServerRequest request) {

        String messageId = request.headers().firstHeader(Constants.X_MESSAGE_ID);

        return request.bodyToMono(CapabilityIdsRequestDTO.class)
                .flatMap(dto -> {

                    Set<ConstraintViolation<CapabilityIdsRequestDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.iterator().next().getMessage();
                        return Mono.error(new BusinessException(errorMessage, TechnicalMessage.INVALID_PARAMETERS));
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> findCapabilitiesServicePort.findByIdIn(dto.ids())
                        .collectList()
                )
                .flatMap(capabilities -> ServerResponse
                        .status(HttpStatus.OK)
                        .bodyValue(capabilities))
                .contextWrite(Context.of(Constants.X_MESSAGE_ID, Objects.requireNonNull(messageId)))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        messageId,
                        ex.getTechnicalMessage(),
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getMessage())
                                .build())
                ))
                .onErrorResume(ex -> {
                    log.error("[findTechnologiesByIds] Unexpected error occurred for messageId: {}", messageId, ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            messageId,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build())
                    );
                });
    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus httpStatus, String identifier,
                                                    TechnicalMessage error, List<ErrorDTO> errors) {
        return Mono.defer(() -> {
            ApiResponseDTO apiErrorResponse = ApiResponseDTO.builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .identifier(identifier)
                    .date(Instant.now().toString())
                    .errors(errors)
                    .build();
            return ServerResponse.status(httpStatus).bodyValue(apiErrorResponse);
        });
    }
}
