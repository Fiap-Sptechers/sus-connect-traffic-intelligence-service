package com.fiap.sus.traffic.infrastructure.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
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
     * Request Interceptor para logar detalhes das requisições Feign.
     * Ajuda a diagnosticar problemas de comunicação com serviços externos.
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.info("🔍 Feign Request: {} {}", template.method(), template.url());
                log.debug("🔍 Feign Headers: {}", template.headers());
                if (template.queries() != null && !template.queries().isEmpty()) {
                    log.info("🔍 Feign Query Params: {}", template.queries());
                }
            }
        };
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
