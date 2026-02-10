package com.fiap.sus.traffic.presentation.dto;

import java.util.UUID;

public record SugestaoResponse(
    UUID unidadeId,
    String nome,
    double scoreFinal,
    double distanciaKm,
    int tempoEstimadoMinutos,
    String razao
) {}
