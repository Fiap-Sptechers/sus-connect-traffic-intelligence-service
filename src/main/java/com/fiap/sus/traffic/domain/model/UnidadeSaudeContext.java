package com.fiap.sus.traffic.domain.model;

import java.util.List;
import java.util.UUID;

public record UnidadeSaudeContext(
    UUID unidadeId,
    String nome,
    Double latitude,
    Double longitude,
    List<String> especialidades,
    IndicadoresOperacionais indicadores,
    Double distanciaKm
) {
    public UnidadeSaudeContext {
        if (unidadeId == null) {
            throw new IllegalArgumentException("unidadeId não pode ser nulo");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser nulo ou vazio");
        }
        // latitude e longitude são opcionais (não são mais obrigatórios)
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("latitude deve estar entre -90 e 90");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("longitude deve estar entre -180 e 180");
        }
        if (especialidades == null) {
            especialidades = List.of();
        }
        if (indicadores == null) {
            indicadores = IndicadoresOperacionais.padrao();
        }
        if (distanciaKm == null) {
            throw new IllegalArgumentException("distanciaKm é obrigatória e deve vir do Network Service");
        }
        if (distanciaKm < 0) {
            throw new IllegalArgumentException("distanciaKm não pode ser negativa");
        }
    }

    public boolean possuiEspecialidade(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            return true; // Se não especificou, todas unidades são válidas
        }
        // Se a lista de especialidades está vazia (não disponível no Network Service),
        // considerar que a unidade possui a especialidade (não filtrar)
        if (especialidades == null || especialidades.isEmpty()) {
            return true; // Não temos dados de especialidades, então não filtramos
        }
        return especialidades.stream()
            .anyMatch(esp -> esp.equalsIgnoreCase(especialidade));
    }
}
