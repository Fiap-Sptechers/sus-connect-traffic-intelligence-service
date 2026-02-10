package com.fiap.sus.traffic.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO intermediário para receber respostas paginadas do Network Service.
 * Usado para deserializar Page<HealthUnitResponse> antes de converter para List<UnidadeSaudeDTO>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponseDTO<T>(
    @JsonProperty("content") List<T> content,
    @JsonProperty("totalElements") long totalElements,
    @JsonProperty("totalPages") int totalPages,
    @JsonProperty("size") int size,
    @JsonProperty("number") int number,
    @JsonProperty("first") boolean first,
    @JsonProperty("last") boolean last,
    @JsonProperty("numberOfElements") int numberOfElements
) {
    public List<T> getContent() {
        return content != null ? content : List.of();
    }
}
