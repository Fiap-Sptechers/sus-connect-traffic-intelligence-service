package com.fiap.sus.traffic.application.port;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;

import java.util.UUID;

public interface LiveOpsServicePort {
    IndicadoresDTO buscarIndicadores(UUID unidadeId);
}
