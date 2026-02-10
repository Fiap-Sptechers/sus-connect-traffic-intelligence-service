package com.fiap.sus.traffic.domain.model;

import com.fiap.sus.traffic.domain.service.AlgoritmoDirecionamentoService;
import lombok.Builder;

import java.util.List;

@Builder
public class MotorDecisao {
    private final List<UnidadeSaudeContext> unidadesCandidatas;
    private final CriterioPeso pesos;
    private final RiskClassification riskClassification;
    private final String especialidadeDesejada;
    private final AlgoritmoDirecionamentoService algoritmoService;

    public MotorDecisao(List<UnidadeSaudeContext> unidadesCandidatas,
                       CriterioPeso pesos,
                       RiskClassification riskClassification,
                       String especialidadeDesejada,
                       AlgoritmoDirecionamentoService algoritmoService) {
        if (unidadesCandidatas == null || unidadesCandidatas.isEmpty()) {
            throw new IllegalArgumentException("unidadesCandidatas não pode ser nulo ou vazio");
        }
        if (pesos == null) {
            throw new IllegalArgumentException("pesos não pode ser nulo");
        }
        if (riskClassification == null) {
            throw new IllegalArgumentException("riskClassification não pode ser nulo");
        }
        if (algoritmoService == null) {
            throw new IllegalArgumentException("algoritmoService não pode ser nulo");
        }
        
        this.unidadesCandidatas = unidadesCandidatas;
        this.pesos = pesos;
        this.riskClassification = riskClassification;
        this.especialidadeDesejada = especialidadeDesejada;
        this.algoritmoService = algoritmoService;
    }

    public List<SugestaoOrdenada> calcularSugestoes(int maxSugestoes) {
        return algoritmoService.calcularSugestoes(
            unidadesCandidatas,
            pesos,
            riskClassification,
            especialidadeDesejada,
            maxSugestoes
        );
    }
}
