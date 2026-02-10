package com.fiap.sus.traffic.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PesosConfigRequest(
    @NotNull(message = "Peso de distância é obrigatório")
    @Min(value = 0, message = "Peso deve estar entre 0.0 e 1.0")
    @Max(value = 1, message = "Peso deve estar entre 0.0 e 1.0")
    Double pesoDistancia,
    
    @NotNull(message = "Peso de TMA é obrigatório")
    @Min(value = 0, message = "Peso deve estar entre 0.0 e 1.0")
    @Max(value = 1, message = "Peso deve estar entre 0.0 e 1.0")
    Double pesoTMA,
    
    @NotNull(message = "Peso de ocupação é obrigatório")
    @Min(value = 0, message = "Peso deve estar entre 0.0 e 1.0")
    @Max(value = 1, message = "Peso deve estar entre 0.0 e 1.0")
    Double pesoOcupacao,
    
    @NotNull(message = "Peso de especialidade é obrigatório")
    @Min(value = 0, message = "Peso deve estar entre 0.0 e 1.0")
    @Max(value = 1, message = "Peso deve estar entre 0.0 e 1.0")
    Double pesoEspecialidade
) {}
