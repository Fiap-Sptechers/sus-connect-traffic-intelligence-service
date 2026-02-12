package com.fiap.sus.traffic.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler seguindo as melhores práticas de Spring Boot.
 * Centraliza o tratamento de todas as exceções da aplicação.
 * 
 * Referência: https://medium.com/@sharmapraveen91/handle-exceptions-in-spring-boot-a-guide-to-clean-code-principles-e8a9d56cafe8
 */
@RestControllerAdvice(basePackages = "com.fiap.sus.traffic.presentation.controller")
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Trata exceções de negócio customizadas.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e, 
            HttpServletRequest request) {
        
        log.warn("Erro de negócio: {} - {}", e.getErrorCode(), e.getMessage());
        
        HttpStatus status = determineHttpStatus(e);
        
        return ResponseEntity.status(status)
            .body(new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                e.getErrorCode(),
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções de validação customizadas.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException e,
            HttpServletRequest request) {
        
        log.warn("Erro de validação: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                e.getMessage(),
                e.getErrorCode(),
                LocalDateTime.now(),
                request.getRequestURI(),
                e.getValidationErrors()
            ));
    }

    /**
     * Trata exceções de recursos não encontrados.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e,
            HttpServletRequest request) {
        
        log.warn("Recurso não encontrado: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                e.getMessage(),
                e.getErrorCode(),
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções de serviços externos.
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException e,
            HttpServletRequest request) {
        
        log.error("Erro ao comunicar com serviço externo {}: {}", e.getServiceName(), e.getMessage(), e);
        
        HttpStatus status = e.getStatusCode() > 0 
            ? HttpStatus.valueOf(e.getStatusCode())
            : HttpStatus.BAD_GATEWAY;
        
        return ResponseEntity.status(status)
            .body(new ErrorResponse(
                status.value(),
                "Erro ao comunicar com serviço externo",
                e.getMessage(),
                e.getErrorCode(),
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções de validação do Spring (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {
        
        Map<String, String> validationErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));
        
        log.warn("Erro de validação de argumentos: {}", validationErrors);
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                "Parâmetros de entrada inválidos",
                "VALIDATION_ERROR",
                LocalDateTime.now(),
                request.getRequestURI(),
                validationErrors
            ));
    }

    /**
     * Trata exceções quando parâmetros obrigatórios estão faltando.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {
        
        log.warn("Parâmetro obrigatório ausente: {}", e.getParameterName());
        
        String message = String.format("Parâmetro obrigatório '%s' não fornecido", e.getParameterName());
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro obrigatório ausente",
                message,
                "MISSING_PARAMETER",
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções quando o tipo do parâmetro está incorreto.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        
        log.warn("Tipo de parâmetro incorreto: {} - esperado: {}", 
            e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "desconhecido");
        
        String message = String.format("Parâmetro '%s' deve ser do tipo %s", 
            e.getName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "válido");
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Tipo de parâmetro inválido",
                message,
                "INVALID_PARAMETER_TYPE",
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções quando o corpo da requisição não pode ser lido (JSON inválido).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {
        
        log.warn("Corpo da requisição inválido: {}", e.getMessage());
        
        String message = "Corpo da requisição inválido ou mal formatado";
        if (e.getMessage() != null && e.getMessage().contains("JSON")) {
            message = "JSON inválido no corpo da requisição";
        }
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição inválido",
                message,
                "INVALID_REQUEST_BODY",
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções de argumentos ilegais (IllegalArgumentException).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {
        
        log.warn("Argumento ilegal: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro inválido",
                e.getMessage(),
                "ILLEGAL_ARGUMENT",
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções de serviços externos (Spring Web Client).
     */
    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<ErrorResponse> handleHttpClientException(
            RuntimeException e,
            HttpServletRequest request) {
        
        HttpStatus status;
        String serviceName = "serviço externo";
        
        if (e instanceof HttpClientErrorException clientError) {
            status = HttpStatus.valueOf(clientError.getStatusCode().value());
            log.error("Erro 4xx do serviço externo: {} - {}", status, clientError.getMessage());
        } else if (e instanceof HttpServerErrorException serverError) {
            status = HttpStatus.valueOf(serverError.getStatusCode().value());
            log.error("Erro 5xx do serviço externo: {} - {}", status, serverError.getMessage());
        } else {
            status = HttpStatus.BAD_GATEWAY;
        }
        
        return ResponseEntity.status(status)
            .body(new ErrorResponse(
                status.value(),
                "Erro ao comunicar com serviço externo",
                String.format("Erro ao comunicar com %s: %s", serviceName, e.getMessage()),
                "EXTERNAL_SERVICE_ERROR",
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Trata exceções genéricas não tratadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e,
            HttpServletRequest request) {
        
        log.error("Erro inesperado na requisição {}: {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now(),
                request.getRequestURI(),
                null
            ));
    }

    /**
     * Determina o HttpStatus apropriado baseado no tipo de exceção de negócio.
     */
    private HttpStatus determineHttpStatus(BusinessException e) {
        if (e instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (e instanceof ExternalServiceException) {
            ExternalServiceException externalEx = (ExternalServiceException) e;
            if (externalEx.getStatusCode() > 0) {
                try {
                    return HttpStatus.valueOf(externalEx.getStatusCode());
                } catch (IllegalArgumentException ignored) {
                    return HttpStatus.BAD_GATEWAY;
                }
            }
            return HttpStatus.BAD_GATEWAY;
        }
        // Default para erros de negócio
        return HttpStatus.BAD_REQUEST;
    }
}
