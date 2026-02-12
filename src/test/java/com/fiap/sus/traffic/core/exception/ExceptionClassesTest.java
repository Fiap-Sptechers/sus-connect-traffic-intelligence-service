package com.fiap.sus.traffic.core.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void deveCriarBusinessExceptionComMensagem() {
        BusinessException exception = new BusinessException("Erro de negócio");

        assertEquals("Erro de negócio", exception.getMessage());
        assertNull(exception.getErrorCode());
    }

    @Test
    void deveCriarBusinessExceptionComErrorCode() {
        BusinessException exception = new BusinessException("ERROR_CODE", "Erro de negócio");

        assertEquals("Erro de negócio", exception.getMessage());
        assertEquals("ERROR_CODE", exception.getErrorCode());
    }

    @Test
    void deveCriarBusinessExceptionComCause() {
        Throwable cause = new RuntimeException("Causa");
        BusinessException exception = new BusinessException("Erro de negócio", cause);

        assertEquals("Erro de negócio", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void deveCriarValidationExceptionComMensagem() {
        ValidationException exception = new ValidationException("Erro de validação");

        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Erro de validação"));
        assertNull(exception.getValidationErrors());
    }

    @Test
    void deveCriarValidationExceptionComCampo() {
        ValidationException exception = new ValidationException("campo", "Campo inválido");

        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("campo"));
        assertNotNull(exception.getValidationErrors());
        assertEquals("Campo inválido", exception.getValidationErrors().get("campo"));
    }

    @Test
    void deveCriarValidationExceptionComMap() {
        Map<String, String> errors = Map.of("campo1", "Erro 1", "campo2", "Erro 2");
        ValidationException exception = new ValidationException(errors);

        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(errors, exception.getValidationErrors());
    }

    @Test
    void deveCriarResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Recurso não encontrado");

        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals("Recurso não encontrado", exception.getMessage());
    }

    @Test
    void deveCriarExternalServiceException() {
        ExternalServiceException exception = new ExternalServiceException(
            "SERVICE_NAME", "Erro no serviço", 500
        );

        assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
        assertEquals("SERVICE_NAME", exception.getServiceName());
        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Erro no serviço"));
    }

    @Test
    void deveCriarErrorResponseCompleto() {
        ErrorResponse response = new ErrorResponse(
            400,
            "Bad Request",
            "Erro de validação",
            "VALIDATION_ERROR",
            java.time.LocalDateTime.now(),
            "/path",
            Map.of("campo", "Erro")
        );

        assertEquals(400, response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Erro de validação", response.message());
        assertEquals("VALIDATION_ERROR", response.errorCode());
        assertNotNull(response.timestamp());
        assertEquals("/path", response.path());
        assertNotNull(response.validationErrors());
        assertEquals("Erro", response.validationErrors().get("campo"));
    }

    @Test
    void deveCriarErrorResponseSimplificado() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Erro");

        assertEquals(400, response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Erro", response.message());
        assertNull(response.errorCode());
    }
}
