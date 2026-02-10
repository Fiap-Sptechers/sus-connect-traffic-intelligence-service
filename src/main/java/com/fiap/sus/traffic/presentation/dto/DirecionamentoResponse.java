package com.fiap.sus.traffic.presentation.dto;

import java.util.List;

public record DirecionamentoResponse(
    List<SugestaoResponse> sugestoes,
    int totalUnidadesAnalisadas,
    long tempoProcessamentoMs
) {}
