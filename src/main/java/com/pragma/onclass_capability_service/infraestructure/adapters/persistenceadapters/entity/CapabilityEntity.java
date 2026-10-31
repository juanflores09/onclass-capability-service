package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("capability")
public class CapabilityEntity {

    @Id
    private Long id;
    private String name;
    private String description;
    private int technologyCount;
}
