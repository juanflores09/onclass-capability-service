package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("capability_technology")
public class CapabilityTechnologyEntity {

    @Id
    private Long id;
    private Long capabilityId;
    private Long technologyId;
}
