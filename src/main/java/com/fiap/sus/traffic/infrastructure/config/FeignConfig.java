package com.fiap.sus.traffic.infrastructure.config;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Configuração de timeout para o Feign Client.
     * Timeout de 3 minutos para processar grandes volumes de dados.
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5, TimeUnit.SECONDS,      // connectTimeout: 5 segundos
            180, TimeUnit.SECONDS,    // readTimeout: 3 minutos
            true                      // followRedirects
        );
    }
}
