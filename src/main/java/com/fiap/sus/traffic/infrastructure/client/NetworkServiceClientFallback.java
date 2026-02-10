package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.PageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
@Slf4j
public class NetworkServiceClientFallback implements NetworkServiceClient {

    @Override
    public PageResponseDTO<HealthUnitResponseDTO> buscarUnidadesProximas(String baseAddress, Double radius, String distanceUnit, int page, int size) {
        log.warn("Fallback: Network Service indisponível. Retornando página vazia para busca por endereço: {}, raio: {} {}", baseAddress, radius, distanceUnit);
        return new PageResponseDTO<>(Collections.emptyList(), 0, 0, size, page, true, true, 0);
    }

    @Override
    public UnidadeSaudeDTO buscarUnidadePorId(UUID id) {
        log.warn("Fallback: Network Service indisponível. Não foi possível buscar unidade {}", id);
        return null;
    }
}
