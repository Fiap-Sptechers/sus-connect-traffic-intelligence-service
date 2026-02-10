package com.fiap.sus.traffic.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuração de observabilidade (métricas, tracing).
 * 
 * Nota: TimedAspect foi removido pois não estamos usando @Timed em nenhum lugar.
 * Se precisar usar @Timed no futuro, adicione a dependência aspectjweaver e
 * descomente o bean timedAspect.
 */
@Configuration
public class ObservabilityConfig {
    
    // TimedAspect removido - não estamos usando @Timed
    // Se precisar no futuro, adicione:
    // 1. Dependência aspectjweaver no pom.xml
    // 2. Descomente o código abaixo:
    //
    // @Bean
    // public TimedAspect timedAspect(MeterRegistry registry) {
    //     return new TimedAspect(registry);
    // }
}
