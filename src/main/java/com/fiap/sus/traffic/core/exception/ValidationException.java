package com.fiap.sus.traffic.core.exception;

import java.util.Map;

/**
 * Exceção lançada quando há erros de validação.
 */
public class ValidationException extends BusinessException {
    
    private final Map<String, String> validationErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = null;
    }
    
    public ValidationException(Map<String, String> validationErrors) {
        super("VALIDATION_ERROR", "Erro de validação nos parâmetros fornecidos");
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", String.format("Erro de validação no campo '%s': %s", field, message));
        this.validationErrors = Map.of(field, message);
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
