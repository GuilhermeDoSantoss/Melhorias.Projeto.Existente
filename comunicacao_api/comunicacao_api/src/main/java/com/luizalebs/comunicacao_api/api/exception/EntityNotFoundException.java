package com.luizalebs.comunicacao_api.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // You can also set the response status here
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, String id) {
        super(String.format("%s com identificador %s não encontrado(a).", entityName, id));
    }
    
    public EntityNotFoundException(String entityName, String fieldName, String fieldValue) {
        super(String.format("%s com %s ''%s'' não encontrado(a).", entityName, fieldName, fieldValue));
    }
} 