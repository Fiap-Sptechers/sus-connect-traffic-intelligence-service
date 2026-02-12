package com.fiap.sus.traffic.presentation.dto;

import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para consulta de direcionamento de paciente")
public record DirecionamentoRequest(
    @Schema(
        description = "Endereço de referência do paciente (ex: 'Av. Paulista, 1000, São Paulo, SP')",
        example = "Av. Paulista, 1000, São Paulo, SP",
        required = true
    )
    @NotBlank(message = "Endereço de referência é obrigatório")
    String baseAddress,
    
    @Schema(
        description = "Classificação de risco do paciente conforme Protocolo Manchester",
        example = "RED",
        allowableValues = {"RED", "ORANGE", "YELLOW", "GREEN", "BLUE"},
        required = true
    )
    @NotBlank(message = "Classificação de risco é obrigatória")
    String riskClassification,
    
    @Schema(
        description = "Especialidade médica desejada (opcional)",
        example = "Cardiologia"
    )
    String especialidade,
    
    @Schema(
        description = "Raio de busca em quilômetros (padrão: 50.0, mínimo: 1.0, máximo: 100.0)",
        example = "10.0",
        minimum = "1.0",
        maximum = "100.0"
    )
    Double radius,
    
    @Schema(
        description = "Unidade de distância para o raio de busca",
        example = "KM",
        allowableValues = {"KM", "METERS", "MILES"},
        defaultValue = "KM"
    )
    String distanceUnit
) {
    /**
     * Converte a String riskClassification para o enum RiskClassification.
     * @return RiskClassification correspondente
     * @throws ValidationException se o valor não for válido
     */
    public RiskClassification getRiskClassificationEnum() {
        if (riskClassification == null || riskClassification.isBlank()) {
            throw new ValidationException("riskClassification", "Classificação de risco é obrigatória");
        }
        try {
            return RiskClassification.valueOf(riskClassification.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("riskClassification",
                String.format("Classificação de risco inválida: %s. Valores válidos: RED, ORANGE, YELLOW, GREEN, BLUE", 
                    riskClassification));
        }
    }
}
