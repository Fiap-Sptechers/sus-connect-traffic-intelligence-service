package com.fiap.sus.traffic.domain.model;

/**
 * Classificação de risco baseada no Protocolo Manchester de Triagem.
 * Utilizado para padronizar a comunicação entre serviços do ecossistema Sus Connect.
 */
public enum RiskClassification {
    RED(1, "Emergência", 0),      // Imediato (0 min)
    ORANGE(2, "Muito Urgente", 10), // Muito Urgente (10 min)
    YELLOW(3, "Urgente", 60),      // Urgente (60 min)
    GREEN(4, "Pouco Urgente", 120), // Pouco Urgente (120 min)
    BLUE(5, "Não Urgente", 240);   // Não Urgente (240 min)

    private final int code;
    private final String description;
    private final int slaMinutes; // Tempo limite de atendimento em minutos

    RiskClassification(int code, String description, int slaMinutes) {
        this.code = code;
        this.description = description;
        this.slaMinutes = slaMinutes;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getSlaMinutes() {
        return slaMinutes;
    }
}
