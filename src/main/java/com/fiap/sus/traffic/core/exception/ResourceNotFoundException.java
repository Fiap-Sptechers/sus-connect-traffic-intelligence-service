package com.fiap.sus.traffic.core.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
    
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super("RESOURCE_NOT_FOUND", 
            String.format("%s não encontrado com identificador: %s", resourceType, identifier));
    }
}
