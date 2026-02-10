package com.fiap.sus.traffic.domain.model;

import java.util.UUID;

public record SugestaoOrdenada(
    UUID unidadeId,
    String nome,
    double scoreFinal,
    double distanciaKm,
    int tempoEstimadoMinutos,
    String razao
) {
    public SugestaoOrdenada {
        if (unidadeId == null) {
            throw new IllegalArgumentException("unidadeId não pode ser nulo");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser nulo ou vazio");
        }
        if (scoreFinal < 0 || scoreFinal > 1.0) {
            throw new IllegalArgumentException("scoreFinal deve estar entre 0.0 e 1.0");
        }
        if (distanciaKm < 0) {
            throw new IllegalArgumentException("distanciaKm não pode ser negativa");
        }
        if (tempoEstimadoMinutos < 0) {
            throw new IllegalArgumentException("tempoEstimadoMinutos não pode ser negativo");
        }
        if (razao == null) {
            razao = "";
        }
    }
}
