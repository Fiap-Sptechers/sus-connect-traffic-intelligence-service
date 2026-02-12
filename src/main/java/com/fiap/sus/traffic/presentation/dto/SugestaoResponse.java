package com.fiap.sus.traffic.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Sugestão de unidade de saúde com score calculado")
public record SugestaoResponse(
    @Schema(description = "ID único da unidade de saúde", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID unidadeId,
    
    @Schema(description = "Nome da unidade de saúde", example = "Hospital Central")
    String nome,
    
    @Schema(description = "Score final calculado (0.0 a 1.0, onde maior é melhor)", example = "0.85")
    double scoreFinal,
    
    @Schema(description = "Distância em quilômetros do endereço de referência", example = "2.5")
    double distanciaKm,
    
    @Schema(description = "Tempo estimado de atendimento em minutos", example = "15")
    int tempoEstimadoMinutos,
    
    @Schema(description = "Justificativa da sugestão", example = "Unidade próxima com baixa ocupação e TMA reduzido")
    String razao
) {}
