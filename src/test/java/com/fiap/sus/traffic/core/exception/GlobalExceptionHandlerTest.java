package com.fiap.sus.traffic.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/direcionamento/consultar");
    }

    @Test
    void deveTratarBusinessException() {
        BusinessException exception = new BusinessException("ERROR_CODE", "Mensagem de erro");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_CODE", response.getBody().errorCode());
        assertEquals("Mensagem de erro", response.getBody().message());
    }

    @Test
    void deveTratarValidationException() {
        ValidationException exception = new ValidationException("campo", "Erro de validação");

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Erro de validação"));
    }

    @Test
    void deveTratarResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Recurso não encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recurso não encontrado", response.getBody().message());
    }

    @Test
    void deveTratarExternalServiceException() {
        ExternalServiceException exception = new ExternalServiceException(
            "SERVICE_NAME", "Erro no serviço", 500
        );

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Erro no serviço"));
    }

    @Test
    void deveTratarMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Erro de validação");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().validationErrors());
    }

    @Test
    void deveTratarMissingServletRequestParameterException() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException(
            "param", "String"
        );

        ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameterException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("param"));
    }

    @Test
    void deveTratarMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("param");
        when(exception.getRequiredType()).thenAnswer(invocation -> String.class);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("param"));
    }

    @Test
    void deveTratarHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("JSON inválido");

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadableException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deveTratarIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().message());
    }

    @Test
    void deveTratarHttpClientErrorException() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Erro 400");

        ResponseEntity<ErrorResponse> response = handler.handleHttpClientException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deveTratarHttpServerErrorException() {
        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro 500");

        ResponseEntity<ErrorResponse> response = handler.handleHttpClientException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deveTratarExceptionGenerica() {
        Exception exception = new Exception("Erro genérico");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().errorCode());
    }
}
