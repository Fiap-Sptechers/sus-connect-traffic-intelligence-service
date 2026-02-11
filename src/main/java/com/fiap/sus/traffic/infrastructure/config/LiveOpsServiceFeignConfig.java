package com.fiap.sus.traffic.infrastructure.config;

import com.fiap.sus.traffic.infrastructure.security.JwtService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração específica para o Feign Client do LiveOps Service.
 * Adiciona autenticação JWT nas requisições.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class LiveOpsServiceFeignConfig {

    private final JwtService jwtService;

    /**
     * Interceptor que adiciona o token JWT no header Authorization
     * para todas as requisições ao LiveOps Service.
     */
    @Bean
    public RequestInterceptor liveOpsServiceRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String token = jwtService.generateToken();
                if (token != null) {
                    template.header("Authorization", "Bearer " + token);
                    log.debug("Token JWT adicionado ao header Authorization para LiveOps Service");
                } else {
                    log.warn("Não foi possível gerar token JWT. Requisição ao LiveOps Service pode falhar com 401.");
                }
            }
        };
    }
}
