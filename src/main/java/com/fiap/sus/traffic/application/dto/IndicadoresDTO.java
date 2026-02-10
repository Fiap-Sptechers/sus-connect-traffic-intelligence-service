package com.fiap.sus.traffic.application.dto;

import com.fiap.sus.traffic.domain.model.RiskClassification;

import java.util.Map;
import java.util.UUID;

public record IndicadoresDTO(
    UUID unidadeId,
    Map<RiskClassification, Integer> tmaPorRisco,
    Integer ocupacaoAtual,
    Integer pacientesEmEspera,
    Integer capacidadeNominal
) {}
