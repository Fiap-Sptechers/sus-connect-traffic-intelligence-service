package com.fiap.sus.traffic.domain.service;

import com.fiap.sus.traffic.domain.model.RiskClassification;
import com.fiap.sus.traffic.domain.model.UnidadeSaudeContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CalculadoraScoreService {

    public double calcularScoreDistancia(List<UnidadeSaudeContext> unidades, UnidadeSaudeContext unidade) {
        if (unidades.isEmpty()) return 1.0;
        
        double maxDistancia = unidades.stream()
            .mapToDouble(u -> u.distanciaKm())
            .max()
            .orElse(1.0);
        
        if (maxDistancia == 0) return 1.0;
        
        // Normalização inversa: menor distância = maior score
        double score = 1.0 - (unidade.distanciaKm() / maxDistancia);
        return Math.max(0.0, Math.min(1.0, score));
    }

    public double calcularScoreTMA(List<UnidadeSaudeContext> unidades, 
                                   UnidadeSaudeContext unidade,
                                   RiskClassification riskClassification) {
        if (unidades.isEmpty()) return 1.0;
        
        Integer tmaUnidade = unidade.indicadores().getTmaPorRisco(riskClassification);
        
        int maxTMA = unidades.stream()
            .mapToInt(u -> u.indicadores().getTmaPorRisco(riskClassification))
            .max()
            .orElse(240); // Máximo baseado no protocolo Manchester (BLUE = 240 min)
        
        if (maxTMA == 0) return 1.0;
        
        // Normalização inversa: menor TMA = maior score
        double score = 1.0 - ((double) tmaUnidade / maxTMA);
        return Math.max(0.0, Math.min(1.0, score));
    }

    public double calcularScoreOcupacao(List<UnidadeSaudeContext> unidades, 
                                        UnidadeSaudeContext unidade) {
        if (unidades.isEmpty()) return 1.0;
        
        double taxaOcupacao = unidade.indicadores().calcularTaxaOcupacao();
        
        double maxOcupacao = unidades.stream()
            .mapToDouble(u -> u.indicadores().calcularTaxaOcupacao())
            .max()
            .orElse(1.0);
        
        if (maxOcupacao == 0) return 1.0;
        
        // Normalização inversa: menor ocupação = maior score
        double score = 1.0 - (taxaOcupacao / maxOcupacao);
        return Math.max(0.0, Math.min(1.0, score));
    }

    public double calcularScoreEspecialidade(UnidadeSaudeContext unidade, String especialidadeDesejada) {
        if (especialidadeDesejada == null || especialidadeDesejada.isBlank()) {
            return 1.0; // Se não especificou, todas unidades são igualmente válidas
        }
        
        return unidade.possuiEspecialidade(especialidadeDesejada) ? 1.0 : 0.0;
    }
}
