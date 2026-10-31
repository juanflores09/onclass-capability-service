package com.pragma.onclass_capability_service.infraestructure.entrypoints.mapper;

import com.pragma.onclass_capability_service.domain.model.Capability;
import com.pragma.onclass_capability_service.domain.model.CapabilityBootcamp;
import com.pragma.onclass_capability_service.domain.model.Technology;
import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.BootcampCapabilitiesResponseDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityRequestDTO;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityMapperTest {

    private CapabilityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CapabilityMapper();
    }

    @Test
    void capabilityToCapabilityEntity_ShouldMapCorrectly() {
        // Given
        Technology tech = new Technology(1L, "Java", "Backend language");
        Capability capability = new Capability(1L, "Backend", "Backend dev", List.of(tech));

        // When
        CapabilityEntity entity = mapper.capabilityToCapabilityEntity(capability);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Backend");
        assertThat(entity.getDescription()).isEqualTo("Backend dev");
        assertThat(entity.getTechnologyCount()).isEqualTo(1);
    }

    @Test
    void capabilityEntityToCapability_ShouldMapCorrectly() {
        // Given
        CapabilityEntity entity = new CapabilityEntity();
        entity.setId(1L);
        entity.setName("Backend");
        entity.setDescription("Backend dev");

        // When
        Capability capability = mapper.capabilityEntityToCapability(entity);

        // Then
        assertThat(capability).isNotNull();
        assertThat(capability.getId()).isEqualTo(1L);
        assertThat(capability.getName()).isEqualTo("Backend");
        assertThat(capability.getDescription()).isEqualTo("Backend dev");
    }

    @Test
    void capabilityEntityToCapability_WithTechnologies_ShouldMapCorrectly() {
        // Given
        CapabilityEntity entity = new CapabilityEntity();
        entity.setId(1L);
        entity.setName("Backend");
        entity.setDescription("Backend dev");

        Technology tech = new Technology(1L, "Java", "Backend language");
        List<Technology> technologies = List.of(tech);

        // When
        Capability capability = mapper.capabilityEntityToCapability(entity, technologies);

        // Then
        assertThat(capability).isNotNull();
        assertThat(capability.getId()).isEqualTo(1L);
        assertThat(capability.getName()).isEqualTo("Backend");
        assertThat(capability.getDescription()).isEqualTo("Backend dev");
        assertThat(capability.getTechnologies()).hasSize(1);
        assertThat(capability.getTechnologies().getFirst().getName()).isEqualTo("Java");
    }

    @Test
    void capabilityToCapabilityResponseDTO_ShouldMapCorrectly() {
        // Given
        Capability capability = new Capability(1L, "Backend", "Backend dev");

        // When
        CapabilityResponseDTO responseDTO = mapper.capabilityToCapabilityResponseDTO(capability);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.id()).isEqualTo(1L);
        assertThat(responseDTO.name()).isEqualTo("Backend");
        assertThat(responseDTO.description()).isEqualTo("Backend dev");
    }

    @Test
    void capabilityToCapabilityResponseDTO_WithTechnologyList_ShouldMapCorrectly() {
        // Given
        Capability capability = new Capability(1L, "Backend", "Backend dev");
        Technology tech = new Technology(1L, "Java", "Backend language");
        List<Technology> technologies = List.of(tech);

        // When
        CapabilityResponseDTO responseDTO = mapper.capabilityToCapabilityResponseDTO(capability, technologies);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.id()).isEqualTo(1L);
        assertThat(responseDTO.name()).isEqualTo("Backend");
        assertThat(responseDTO.description()).isEqualTo("Backend dev");
        assertThat(responseDTO.technologies()).hasSize(1);
        assertThat(responseDTO.technologies().getFirst().name()).isEqualTo("Java");
    }

    @Test
    void capabilityRequestDTOToCapability_ShouldMapCorrectly() {
        // Given
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(
                "Backend",
                "Backend dev",
                List.of(1L, 2L)
        );

        // When
        Capability capability = mapper.capabilityRequestDTOToCapability(requestDTO);

        // Then
        assertThat(capability).isNotNull();
        assertThat(capability.getName()).isEqualTo("Backend");
        assertThat(capability.getDescription()).isEqualTo("Backend dev");
        assertThat(capability.getTechnologies()).hasSize(2);
        assertThat(capability.getTechnologies().get(0).getId()).isEqualTo(1L);
        assertThat(capability.getTechnologies().get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toResponseDTO_ShouldReturnEmptyList_WhenInputIsNull() {
        // When
        List<BootcampCapabilitiesResponseDTO> result = mapper.toResponseDTO(null);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void toResponseDTO_ShouldMapCorrectly_WhenInputIsValid() {
        // Given
        Technology tech = new Technology(1L, "Java", "Language");
        Capability capability = new Capability(1L, "Backend", "Dev", List.of(tech));
        CapabilityBootcamp bootcamp = new CapabilityBootcamp(100L, List.of(capability));

        // When
        List<BootcampCapabilitiesResponseDTO> result = mapper.toResponseDTO(List.of(bootcamp));

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().bootcampId()).isEqualTo(100L);
        assertThat(result.getFirst().capabilities()).hasSize(1);
        assertThat(result.getFirst().capabilities().getFirst().name()).isEqualTo("Backend");
        assertThat(result.getFirst().capabilities().getFirst().technologies()).hasSize(1);
        assertThat(result.getFirst().capabilities().getFirst().technologies().getFirst().name()).isEqualTo("Java");
    }
}
