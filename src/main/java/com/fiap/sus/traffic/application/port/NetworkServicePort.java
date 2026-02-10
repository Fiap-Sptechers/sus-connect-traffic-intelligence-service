package com.fiap.sus.traffic.application.port;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;

import java.util.List;
import java.util.UUID;

public interface NetworkServicePort {
    List<UnidadeSaudeDTO> buscarUnidadesProximas(String baseAddress, Double radius, String distanceUnit);
    UnidadeSaudeDTO buscarUnidadePorId(UUID id);
}
