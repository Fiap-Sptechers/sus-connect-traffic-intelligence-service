package com.fiap.sus.traffic.domain.model;

import com.fiap.sus.traffic.core.exception.ValidationException;
import lombok.Builder;

@Builder
public record CriterioPeso(
    double pesoDistancia,
    double pesoTMA,
    double pesoOcupacao,
    double pesoEspecialidade
) {
    public CriterioPeso {
        validate(pesoDistancia, pesoTMA, pesoOcupacao, pesoEspecialidade);
    }

    private static void validate(double pesoDistancia, double pesoTMA, 
                                double pesoOcupacao, double pesoEspecialidade) {
        if (pesoDistancia < 0 || pesoDistancia > 1.0) {
            throw new ValidationException("pesoDistancia", "Peso deve estar entre 0.0 e 1.0");
        }
        if (pesoTMA < 0 || pesoTMA > 1.0) {
            throw new ValidationException("pesoTMA", "Peso deve estar entre 0.0 e 1.0");
        }
        if (pesoOcupacao < 0 || pesoOcupacao > 1.0) {
            throw new ValidationException("pesoOcupacao", "Peso deve estar entre 0.0 e 1.0");
        }
        if (pesoEspecialidade < 0 || pesoEspecialidade > 1.0) {
            throw new ValidationException("pesoEspecialidade", "Peso deve estar entre 0.0 e 1.0");
        }
        
        double soma = pesoDistancia + pesoTMA + pesoOcupacao + pesoEspecialidade;
        if (soma > 1.0) {
            throw new ValidationException(
                String.format("Soma dos pesos (%.2f) não pode ser maior que 1.0", soma)
            );
        }
    }

    public static CriterioPeso padrao() {
        return new CriterioPeso(0.3, 0.4, 0.2, 0.1);
    }
}
