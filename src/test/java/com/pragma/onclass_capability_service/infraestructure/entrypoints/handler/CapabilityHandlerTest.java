package com.pragma.onclass_capability_service.infraestructure.entrypoints.handler;

import com.pragma.onclass_capability_service.domain.api.CreateCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import com.pragma.onclass_capability_service.domain.exceptions.BusinessException;
import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.PageResult;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityIdsRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityResponseDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.util.Constants;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapabilityHandlerTest {

    @Mock
    private CreateCapabilityServicePort createCapabilityServicePort;

    @Mock
    private FindCapabilitiesServicePort findCapabilitiesServicePort;

    @Mock
    private CapabilityMapper capabilityMapper;

    @Mock
    private Validator validator;

    @Mock
    private ServerRequest serverRequest;

    @Mock
    private ServerRequest.Headers headers;

    @InjectMocks
    private CapabilityHandler capabilityHandler;

    @BeforeEach
    void setUp() {
        lenient().when(serverRequest.headers()).thenReturn(headers);
        lenient().when(headers.firstHeader(Constants.X_MESSAGE_ID)).thenReturn("msg-12345");
    }

    @Test
    void createCapability_Success() {
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO("Backend", "Desc", List.of(1L, 2L));
        Capability capability = new Capability();
        CapabilityResponseDTO responseDTO = new CapabilityResponseDTO(1L, "Backend", "Desc");

        when(serverRequest.bodyToMono(CapabilityRequestDTO.class)).thenReturn(Mono.just(requestDTO));
        when(validator.validate(any(CapabilityRequestDTO.class))).thenReturn(Set.of());
        when(capabilityMapper.capabilityRequestDTOToCapability(requestDTO)).thenReturn(capability);
        when(createCapabilityServicePort.createCapability(capability, "msg-12345")).thenReturn(Mono.just(capability));
        when(capabilityMapper.capabilityResponseDTOToCapability(capability)).thenReturn(responseDTO);

        Mono<ServerResponse> responseMono = capabilityHandler.createCapability(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.CREATED))
                .verifyComplete();

        verify(createCapabilityServicePort, times(1)).createCapability(capability, "msg-12345");
    }

    @Test
    void createCapability_DuplicateTechnologies_ReturnsBadRequest() {
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO("Backend", "Desc", List.of(1L, 1L));

        when(serverRequest.bodyToMono(CapabilityRequestDTO.class)).thenReturn(Mono.just(requestDTO));
        when(validator.validate(any(CapabilityRequestDTO.class))).thenReturn(Set.of());

        Mono<ServerResponse> responseMono = capabilityHandler.createCapability(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.BAD_REQUEST))
                .verifyComplete();

        verify(createCapabilityServicePort, never()).createCapability(any(), any());
    }

    @Test
    void createCapability_BusinessException_ReturnsBadRequest() {
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO("Backend", "Desc", List.of(1L));
        Capability capability = new Capability();

        when(serverRequest.bodyToMono(CapabilityRequestDTO.class)).thenReturn(Mono.just(requestDTO));
        when(validator.validate(any(CapabilityRequestDTO.class))).thenReturn(Set.of());
        when(capabilityMapper.capabilityRequestDTOToCapability(requestDTO)).thenReturn(capability);
        when(createCapabilityServicePort.createCapability(capability, "msg-12345"))
                .thenReturn(Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_ALREADY_EXISTS)));

        Mono<ServerResponse> responseMono = capabilityHandler.createCapability(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.BAD_REQUEST))
                .verifyComplete();
    }

    @Test
    void createCapability_UnexpectedException_ReturnsInternalServerError() {
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO("Backend", "Desc", List.of(1L));
        Capability capability = new Capability();

        when(serverRequest.bodyToMono(CapabilityRequestDTO.class)).thenReturn(Mono.just(requestDTO));
        when(validator.validate(any(CapabilityRequestDTO.class))).thenReturn(Set.of());
        when(capabilityMapper.capabilityRequestDTOToCapability(requestDTO)).thenReturn(capability);
        when(createCapabilityServicePort.createCapability(capability, "msg-12345"))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

        Mono<ServerResponse> responseMono = capabilityHandler.createCapability(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR))
                .verifyComplete();
    }

    @Test
    void listCapabilities_Success() {
        when(serverRequest.queryParam("page")).thenReturn(Optional.of("0"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.of("name"));
        when(serverRequest.queryParam("direction")).thenReturn(Optional.of("ASC"));

        Capability capability = new Capability();
        PageResult<Capability> pageResult = new PageResult<>(List.of(capability), 1L);
        CapabilityResponseDTO responseDTO = new CapabilityResponseDTO(1L, "Backend", "Desc");

        when(findCapabilitiesServicePort.listCapabilities(0, 10, "name", "ASC"))
                .thenReturn(Mono.just(pageResult));
        when(capabilityMapper.capabilityResponseDTOToCapability(eq(capability), any())).thenReturn(responseDTO);

        Mono<ServerResponse> responseMono = capabilityHandler.listCapabilities(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
                .verifyComplete();

        verify(findCapabilitiesServicePort, times(1)).listCapabilities(0, 10, "name", "ASC");
    }

    @Test
    void validateCapabilities_Success() {
        CapabilityIdsRequestDTO requestDTO = new CapabilityIdsRequestDTO(List.of(1L, 2L));

        when(serverRequest.bodyToMono(CapabilityIdsRequestDTO.class)).thenReturn(Mono.just(requestDTO));
        when(validator.validate(any(CapabilityIdsRequestDTO.class))).thenReturn(Set.of());
        when(findCapabilitiesServicePort.areAllCapabilitiesExist(requestDTO.ids())).thenReturn(Mono.just(true));

        Mono<ServerResponse> responseMono = capabilityHandler.validateCapabilities(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
                .verifyComplete();

        verify(findCapabilitiesServicePort, times(1)).areAllCapabilitiesExist(requestDTO.ids());
    }

    @Test
    void findCapabilitiesByIds_Success() {
        CapabilityIdsRequestDTO requestDTO = new CapabilityIdsRequestDTO(List.of(1L));
        Capability capability = new Capability();

        when(serverRequest.bodyToMono(CapabilityIdsRequestDTO.class)).thenReturn(Mono.just(requestDTO));
        when(validator.validate(any(CapabilityIdsRequestDTO.class))).thenReturn(Set.of());
        when(findCapabilitiesServicePort.findByIdIn(requestDTO.ids())).thenReturn(Flux.just(capability));

        Mono<ServerResponse> responseMono = capabilityHandler.findCapabilitiesByIds(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
                .verifyComplete();

        verify(findCapabilitiesServicePort, times(1)).findByIdIn(requestDTO.ids());
    }
}
