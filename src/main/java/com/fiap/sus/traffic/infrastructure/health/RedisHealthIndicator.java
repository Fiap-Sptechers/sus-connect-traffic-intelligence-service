package com.fiap.sus.traffic.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            var connection = connectionFactory.getConnection();
            connection.ping();
            connection.close();
            return Health.up()
                .withDetail("status", "Redis está disponível")
                .build();
        } catch (Exception e) {
            // Não marcar como DOWN para não bloquear o health check principal
            // Redis pode estar temporariamente indisponível mas a aplicação pode funcionar
            return Health.unknown()
                .withDetail("status", "Redis não acessível")
                .withDetail("error", e.getMessage())
                .withDetail("note", "Aplicação pode funcionar sem Redis (cache desabilitado)")
                .build();
        }
    }
}
