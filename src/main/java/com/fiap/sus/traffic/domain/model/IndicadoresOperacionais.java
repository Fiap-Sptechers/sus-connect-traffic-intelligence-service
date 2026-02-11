package com.fiap.sus.traffic.domain.model;

import java.util.Map;

/**
 * Indicadores operacionais de uma unidade de saúde.
 * Utiliza o protocolo Manchester com 5 níveis de classificação de risco.
 */
public record IndicadoresOperacionais(
    Integer tmaRed,           // minutos - Emergência
    Integer tmaOrange,        // minutos - Muito Urgente
    Integer tmaYellow,        // minutos - Urgente
    Integer tmaGreen,          // minutos - Pouco Urgente
    Integer tmaBlue,           // minutos - Não Urgente
    Integer ocupacaoAtual,     // pacientes
    Integer pacientesEmEspera,
    Integer capacidadeNominal
) {
    public IndicadoresOperacionais {
        // Valores padrão conservadores baseados no protocolo Manchester
        if (tmaRed == null) tmaRed = 5;      // Emergência: 0 min (tolerância 5 min)
        if (tmaOrange == null) tmaOrange = 10; // Muito Urgente: 10 min
        if (tmaYellow == null) tmaYellow = 60;  // Urgente: 60 min
        if (tmaGreen == null) tmaGreen = 120;   // Pouco Urgente: 120 min
        if (tmaBlue == null) tmaBlue = 240;     // Não Urgente: 240 min
        if (ocupacaoAtual == null) ocupacaoAtual = 0;
        if (pacientesEmEspera == null) pacientesEmEspera = 0;
        if (capacidadeNominal == null) capacidadeNominal = 1; // evitar divisão por zero
    }

    public Integer getTmaPorRisco(RiskClassification risco) {
        return switch (risco) {
            case RED -> tmaRed;
            case ORANGE -> tmaOrange;
            case YELLOW -> tmaYellow;
            case GREEN -> tmaGreen;
            case BLUE -> tmaBlue;
        };
    }

    public double calcularTaxaOcupacao() {
        if (capacidadeNominal == 0) return 1.0;
        return Math.min(1.0, (double) ocupacaoAtual / capacidadeNominal);
    }

    public static IndicadoresOperacionais padrao() {
        return new IndicadoresOperacionais(5, 10, 60, 120, 240, 0, 0, 10);
    }

    public static IndicadoresOperacionais fromMap(Map<RiskClassification, Integer> tmaPorRisco,
                                                   Integer ocupacaoAtual,
                                                   Integer pacientesEmEspera,
                                                   Integer capacidadeNominal) {
        // Se tmaPorRisco for null, usar valores padrão
        if (tmaPorRisco == null) {
            return new IndicadoresOperacionais(
                5,    // RED
                10,   // ORANGE
                60,   // YELLOW
                120,  // GREEN
                240,  // BLUE
                ocupacaoAtual != null ? ocupacaoAtual : 0,
                pacientesEmEspera != null ? pacientesEmEspera : 0,
                capacidadeNominal != null ? capacidadeNominal : 10
            );
        }
        
        return new IndicadoresOperacionais(
            tmaPorRisco.getOrDefault(RiskClassification.RED, 5),
            tmaPorRisco.getOrDefault(RiskClassification.ORANGE, 10),
            tmaPorRisco.getOrDefault(RiskClassification.YELLOW, 60),
            tmaPorRisco.getOrDefault(RiskClassification.GREEN, 120),
            tmaPorRisco.getOrDefault(RiskClassification.BLUE, 240),
            ocupacaoAtual != null ? ocupacaoAtual : 0,
            pacientesEmEspera != null ? pacientesEmEspera : 0,
            capacidadeNominal != null ? capacidadeNominal : 10
        );
    }
}
