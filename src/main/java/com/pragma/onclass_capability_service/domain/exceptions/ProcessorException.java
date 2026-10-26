package com.pragma.onclass_capability_service.domain.exceptions;

import com.pragma.onclass_capability_service.domain.enums.TechnicalMessage;
import lombok.Getter;

@Getter
public class ProcessorException extends RuntimeException{

    private final TechnicalMessage technicalMessage;

    public ProcessorException(Throwable cause, TechnicalMessage message){
        super(cause);
        this.technicalMessage = message;
    }

    public ProcessorException(String message, TechnicalMessage technicalMessage){
        super(message);
        this.technicalMessage = technicalMessage;
    }
}
