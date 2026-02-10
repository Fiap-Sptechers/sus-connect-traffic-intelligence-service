package com.fiap.sus.traffic.domain.service;

import com.fiap.sus.traffic.domain.model.RiskClassification;
import com.fiap.sus.traffic.domain.model.CriterioPeso;
import com.fiap.sus.traffic.domain.model.SugestaoOrdenada;
import com.fiap.sus.traffic.domain.model.UnidadeSaudeContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlgoritmoDirecionamentoService {

    private final CalculadoraScoreService calculadoraScore;

    public List<SugestaoOrdenada> calcularSugestoes(
            List<UnidadeSaudeContext> unidades,
            CriterioPeso pesos,
            RiskClassification riskClassification,
            String especialidadeDesejada,
            int maxSugestoes) {

        log.info("Calculando sugestões para {} unidades, risco: {}, especialidade: {}", 
            unidades.size(), riskClassification, especialidadeDesejada);

        long unidadesAntesFiltro = unidades.size();
        List<SugestaoOrdenada> sugestoes = unidades.stream()
            .filter(u -> u.possuiEspecialidade(especialidadeDesejada))
            .map(unidade -> calcularScoreFinal(unidade, unidades, pesos, riskClassification, especialidadeDesejada))
            .sorted(Comparator.comparing(SugestaoOrdenada::scoreFinal).reversed())
            .limit(maxSugestoes)
            .collect(Collectors.toList());

        long unidadesAposFiltro = unidades.stream()
            .filter(u -> u.possuiEspecialidade(especialidadeDesejada))
            .count();
        
        log.info("Geradas {} sugestões ({} unidades após filtro de especialidade)", 
            sugestoes.size(), unidadesAposFiltro);
        
        if (sugestoes.isEmpty() && unidadesAntesFiltro > 0) {
            log.warn("Nenhuma sugestão gerada apesar de {} unidades disponíveis. Verifique filtros de especialidade.", 
                unidadesAntesFiltro);
        }
        return sugestoes;
    }

    private SugestaoOrdenada calcularScoreFinal(
            UnidadeSaudeContext unidade,
            List<UnidadeSaudeContext> todasUnidades,
            CriterioPeso pesos,
            RiskClassification riskClassification,
            String especialidadeDesejada) {

        double scoreDistancia = calculadoraScore.calcularScoreDistancia(todasUnidades, unidade);
        double scoreTMA = calculadoraScore.calcularScoreTMA(todasUnidades, unidade, riskClassification);
        double scoreOcupacao = calculadoraScore.calcularScoreOcupacao(todasUnidades, unidade);
        double scoreEspecialidade = calculadoraScore.calcularScoreEspecialidade(unidade, especialidadeDesejada);

        double scoreFinal = (pesos.pesoDistancia() * scoreDistancia) +
                           (pesos.pesoTMA() * scoreTMA) +
                           (pesos.pesoOcupacao() * scoreOcupacao) +
                           (pesos.pesoEspecialidade() * scoreEspecialidade);

        // Normalizar para garantir que está entre 0 e 1
        scoreFinal = Math.max(0.0, Math.min(1.0, scoreFinal));

        Integer tma = unidade.indicadores().getTmaPorRisco(riskClassification);
        int tempoEstimado = (int) (unidade.distanciaKm() * 2) + tma; // 2 min/km + TMA

        String razao = construirRazao(scoreDistancia, scoreTMA, scoreOcupacao, scoreEspecialidade, 
                                      unidade.distanciaKm(), tma);

        return new SugestaoOrdenada(
            unidade.unidadeId(),
            unidade.nome(),
            scoreFinal,
            unidade.distanciaKm(),
            tempoEstimado,
            razao
        );
    }

    private String construirRazao(double scoreDistancia, double scoreTMA, double scoreOcupacao,
                                 double scoreEspecialidade, double distanciaKm, int tma) {
        StringBuilder razao = new StringBuilder();
        
        if (scoreDistancia > 0.7) {
            razao.append("Próxima (").append(String.format("%.1f", distanciaKm)).append(" km). ");
        }
        if (scoreTMA > 0.7) {
            razao.append("TMA rápido (").append(tma).append(" min). ");
        }
        if (scoreOcupacao > 0.7) {
            razao.append("Baixa ocupação. ");
        }
        if (scoreEspecialidade == 1.0) {
            razao.append("Possui especialidade necessária. ");
        }
        
        return razao.toString().trim();
    }
}
