package com.fiap.sus.traffic.infrastructure.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Error Decoder customizado para Feign que lança exceções compatíveis com Resilience4j Circuit Breaker.
 * 
 * O Circuit Breaker do Resilience4j reconhece automaticamente:
 * - FeignException e suas subclasses
 * - HttpServerErrorException (Spring)
 * - HttpClientErrorException (Spring)
 * - Exceções de I/O (ConnectException, TimeoutException, etc.)
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Erro na chamada Feign: {} - Status: {}", methodKey, response.status());
        
        // Para erros 4xx, usar HttpClientErrorException (reconhecida pelo Circuit Breaker)
        if (response.status() >= 400 && response.status() < 500) {
            return new HttpClientErrorException(
                org.springframework.http.HttpStatus.valueOf(response.status()),
                "Erro do cliente: " + response.status()
            );
        }
        
        // Para erros 5xx, usar HttpServerErrorException (reconhecida pelo Circuit Breaker)
        if (response.status() >= 500) {
            return new HttpServerErrorException(
                org.springframework.http.HttpStatus.valueOf(response.status()),
                "Erro do servidor: " + response.status()
            );
        }
        
        // Para outros casos, usar o decoder padrão do Feign
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
