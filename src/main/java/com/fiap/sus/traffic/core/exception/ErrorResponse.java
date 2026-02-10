package com.fiap.sus.traffic.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Resposta padronizada de erro da API.
 * Segue o padrão RFC 7807 (Problem Details for HTTP APIs).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String error,
    String message,
    String errorCode,
    LocalDateTime timestamp,
    String path,
    Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, null, LocalDateTime.now(), null, null);
    }
    
    public ErrorResponse(int status, String error, String message, String errorCode) {
        this(status, error, message, errorCode, LocalDateTime.now(), null, null);
    }
    
    public ErrorResponse(int status, String error, String message, String errorCode, Map<String, String> validationErrors) {
        this(status, error, message, errorCode, LocalDateTime.now(), null, validationErrors);
    }
}
