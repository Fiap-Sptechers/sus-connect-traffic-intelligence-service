package com.fiap.sus.traffic.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta da consulta de direcionamento com sugestões ordenadas por score")
public record DirecionamentoResponse(
    @Schema(description = "Lista de sugestões de unidades de saúde ordenadas por score (maior = melhor)")
    List<SugestaoResponse> sugestoes,
    
    @Schema(description = "Total de unidades analisadas e retornadas", example = "1")
    int totalUnidadesAnalisadas,
    
    @Schema(description = "Tempo de processamento da consulta em milissegundos", example = "125")
    long tempoProcessamentoMs
) {}
