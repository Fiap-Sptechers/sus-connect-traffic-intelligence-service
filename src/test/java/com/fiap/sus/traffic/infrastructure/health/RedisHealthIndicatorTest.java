package com.fiap.sus.traffic.infrastructure.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisHealthIndicatorTest {

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection connection;

    private RedisHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new RedisHealthIndicator(connectionFactory);
    }

    @Test
    void deveRetornarHealthUpQuandoRedisDisponivel() throws Exception {
        when(connectionFactory.getConnection()).thenReturn(connection);
        // ping() retorna String, não void
        when(connection.ping()).thenReturn("PONG");

        Health health = healthIndicator.health();

        assertEquals(Health.up().build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("status"));
        verify(connection).ping();
        verify(connection).close();
    }

    @Test
    void deveRetornarHealthUnknownQuandoRedisIndisponivel() throws Exception {
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        Health health = healthIndicator.health();

        assertEquals(Health.unknown().build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("status"));
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    void deveRetornarHealthUnknownQuandoPingFalha() throws Exception {
        when(connectionFactory.getConnection()).thenReturn(connection);
        doThrow(new RuntimeException("Ping failed")).when(connection).ping();

        Health health = healthIndicator.health();

        assertEquals(Health.unknown().build().getStatus(), health.getStatus());
    }
}
