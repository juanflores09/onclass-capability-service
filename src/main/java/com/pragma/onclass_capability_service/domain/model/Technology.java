package com.pragma.onclass_capability_service.domain.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Technology {

    private Long id;
    private String name;
    private String description;

    public Technology(Long id) {
        this.id = id;
    }
}
