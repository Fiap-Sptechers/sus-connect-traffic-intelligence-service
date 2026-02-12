package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.PageResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NetworkServiceClientFallbackTest {

    private final NetworkServiceClientFallback fallback = new NetworkServiceClientFallback();

    @Test
    void deveRetornarPaginaVaziaQuandoBuscarUnidadesProximas() {
        PageResponseDTO<HealthUnitResponseDTO> result = fallback.buscarUnidadesProximas(
            "Rua Teste, 123",
            10.0,
            "KM",
            0,
            100
        );

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
    }

    @Test
    void deveRetornarNullQuandoBuscarUnidadePorId() {
        UUID id = UUID.randomUUID();
        UnidadeSaudeDTO result = fallback.buscarUnidadePorId(id);

        assertNull(result);
    }
}
