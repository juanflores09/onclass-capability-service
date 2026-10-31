package com.pragma.onclass_capability_service.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TechnicalMessage {

    INTERNAL_ERROR("500", "Something went wrong, please try again"),
    INTERNAL_ERROR_IN_ADAPTERS("500", "Something went wrong in adapters, please try again"),
    CAPABILITY_ALREADY_EXISTS("400", "Ya existe una capacidad con ese nombre"),
    INVALID_PARAMETERS("400", "Parámetros inválidos"),
    DUPLICATE_TECHNOLOGIES("400", "La capacidad no puede contener tecnologías repetidas" ),
    TECHNOLOGY_NOT_FOUND("400", "Una o más tecnologías no existen"),
    EXTERNAL_SERVICE_ERROR("500", "Error al comunicarse con el servicio externo de tecnologías");

    private final String code;
    private final String message;

}
