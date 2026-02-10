package com.fiap.sus.traffic.presentation.dto;

import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import jakarta.validation.constraints.NotBlank;

public record DirecionamentoRequest(
    @NotBlank(message = "Endereço de referência é obrigatório")
    String baseAddress,
    
    @NotBlank(message = "Classificação de risco é obrigatória")
    String riskClassification,
    
    String especialidade,
    
    Double radius,
    
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
