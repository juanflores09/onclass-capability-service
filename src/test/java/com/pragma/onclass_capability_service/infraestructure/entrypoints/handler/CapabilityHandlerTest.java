package com.pragma.onclass_capability_service.infraestructure.entrypoints.handler;

import com.pragma.onclass_capability_service.domain.api.CreateCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.api.FindCapabilitiesServicePort;
import com.pragma.onclass_capability_service.domain.api.SaveBootcampCapabilityServicePort;
import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import com.pragma.onclass_capability_service.domain.model.PageResult;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.*;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper.CapabilityMapper;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.util.Constants;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapabilityHandlerTest {

    @Mock
    private CreateCapabilityServicePort capabilityServicePort;

    @Mock
    private FindCapabilitiesServicePort findCapabilitiesServicePort;

    @Mock
    private SaveBootcampCapabilityServicePort saveBootcampCapabilityServicePort;

    @Mock
    private CapabilityMapper capabilityMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private CapabilityHandler handler;

    @Test
    void createCapability_ShouldReturnCreatedResponse() {
        // Given
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO("Backend", "Backend dev", List.of(1L, 2L));
        Capability capability = new Capability(1L, "Backend", "Backend dev");
        CapabilityResponseDTO responseDTO = new CapabilityResponseDTO(1L, "Backend", "Backend dev", Collections.emptyList());

        MockServerRequest request = MockServerRequest.builder()
                .header(Constants.X_MESSAGE_ID, "msg-123")
                .body(Mono.just(requestDTO));

        when(validator.validate(requestDTO)).thenReturn(Collections.emptySet());
        when(capabilityMapper.capabilityRequestDTOToCapability(requestDTO)).thenReturn(capability);
        when(capabilityServicePort.createCapability(capability, "msg-123")).thenReturn(Mono.just(capability));
        when(capabilityMapper.capabilityToCapabilityResponseDTO(capability)).thenReturn(responseDTO);

        // When & Then
        StepVerifier.create(handler.createCapability(request))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.CREATED)
                .verifyComplete();

        verify(validator).validate(requestDTO);
        verify(capabilityServicePort).createCapability(capability, "msg-123");
    }

    @Test
    void createCapability_ShouldReturnBadRequest_WhenDuplicateTechnologies() {
        // Given - Tecnologías duplicadas
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO("Backend", "Backend dev", List.of(1L, 1L));

        MockServerRequest request = MockServerRequest.builder()
                .header(Constants.X_MESSAGE_ID, "msg-123")
                .body(Mono.just(requestDTO));

        when(validator.validate(requestDTO)).thenReturn(Collections.emptySet());

        // When & Then
        StepVerifier.create(handler.createCapability(request))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    void listCapabilities_ShouldReturnOkWithPagedResponse() {
        // Given
        Capability capability = new Capability(1L, "Backend", "Backend dev");
        CapabilityResponseDTO responseDTO = new CapabilityResponseDTO(1L, "Backend", "Backend dev", Collections.emptyList());
        PageResult<Capability> pageResult = new PageResult<>(List.of(capability), 1L);

        MockServerRequest request = MockServerRequest.builder()
                .header(Constants.X_MESSAGE_ID, "msg-123")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sortBy", "name")
                .queryParam("direction", "ASC")
                .build();

        when(findCapabilitiesServicePort.listCapabilities(0, 10, "name", "ASC", "msg-123"))
                .thenReturn(Mono.just(pageResult));
        when(capabilityMapper.capabilityToCapabilityResponseDTO(eq(capability), any())).thenReturn(responseDTO);

        // When & Then
        StepVerifier.create(handler.listCapabilities(request))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(findCapabilitiesServicePort).listCapabilities(0, 10, "name", "ASC", "msg-123");
    }

    @Test
    void saveBootcampCapabilities_ShouldReturnCreatedStatus() {
        // Given
        BootcampCapabilitiesRequestDTO requestDTO = new BootcampCapabilitiesRequestDTO(100L, List.of(1L, 2L));

        MockServerRequest request = MockServerRequest.builder()
                .header(Constants.X_MESSAGE_ID, "msg-123")
                .body(Mono.just(requestDTO));

        when(validator.validate(requestDTO)).thenReturn(Collections.emptySet());
        when(saveBootcampCapabilityServicePort.saveAll(100L, List.of(1L, 2L))).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(handler.saveBootcampCapabilities(request))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.CREATED)
                .verifyComplete();

        verify(saveBootcampCapabilityServicePort).saveAll(100L, List.of(1L, 2L));
    }

    @Test
    void findCapabilitiesByBootcampIds_ShouldReturnOkResponse() {
        // Given
        CapabilityIdsRequestDTO requestDTO = new CapabilityIdsRequestDTO(List.of(100L));
        CapabilityBootcamp bootcamp = new CapabilityBootcamp(100L, Collections.emptyList());
        List<BootcampCapabilitiesResponseDTO> responseDTOs = List.of(new BootcampCapabilitiesResponseDTO(100L, Collections.emptyList()));

        MockServerRequest request = MockServerRequest.builder()
                .header(Constants.X_MESSAGE_ID, "msg-123")
                .body(Mono.just(requestDTO));

        when(validator.validate(requestDTO)).thenReturn(Collections.emptySet());
        when(findCapabilitiesServicePort.findCapabilitiesByBootcampIds(List.of(100L), "msg-123"))
                .thenReturn(Flux.just(bootcamp));
        when(capabilityMapper.toResponseDTO(anyList())).thenReturn(responseDTOs);

        // When & Then
        StepVerifier.create(handler.findCapabilitiesByBootcampIds(request))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(findCapabilitiesServicePort).findCapabilitiesByBootcampIds(List.of(100L), "msg-123");
        verify(capabilityMapper).toResponseDTO(anyList());
    }
}
