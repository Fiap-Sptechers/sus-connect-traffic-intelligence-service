package com.fiap.sus.traffic.domain.service;

import com.fiap.sus.traffic.domain.model.RiskClassification;
import com.fiap.sus.traffic.domain.model.IndicadoresOperacionais;
import com.fiap.sus.traffic.domain.model.UnidadeSaudeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraScoreServiceTest {

    private CalculadoraScoreService calculadora;
    private List<UnidadeSaudeContext> unidades;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraScoreService();
        
        // Criar unidades com valores que garantam scores > 0.5
        // Unidade 0: menor distância (1.0), menor TMA (20), menor ocupação (0.3)
        // Unidade 1: distância média (5.0), TMA média (40), ocupação média (0.7)
        // Unidade 2: maior distância (10.0), maior TMA (60), maior ocupação (0.9)
        unidades = List.of(
            criarUnidade(1.0, 20, 0.3),
            criarUnidade(5.0, 40, 0.7),
            criarUnidade(10.0, 60, 0.9)
        );
    }

    @Test
    void deveCalcularScoreDistanciaCorretamente() {
        UnidadeSaudeContext unidade = unidades.get(0); // Menor distância
        
        double score = calculadora.calcularScoreDistancia(unidades, unidade);
        
        assertTrue(score > 0.5); // Deve ter score alto por ser a mais próxima
        assertTrue(score <= 1.0);
    }

    @Test
    void deveCalcularScoreTMACorretamente() {
        UnidadeSaudeContext unidade = unidades.get(0); // Menor TMA (YELLOW = 20 + 10 = 30 min)
        
        double score = calculadora.calcularScoreTMA(unidades, unidade, RiskClassification.YELLOW);
        
        // Com TMA YELLOW=30 e maxTMA YELLOW=70, score = 1.0 - (30/70) ≈ 0.57
        assertTrue(score > 0.5, "Score deve ser > 0.5, mas foi " + score);
        assertTrue(score <= 1.0);
        // Verificar que a unidade com menor TMA tem score maior que as outras
        double scoreUnidade1 = calculadora.calcularScoreTMA(unidades, unidades.get(1), RiskClassification.YELLOW);
        double scoreUnidade2 = calculadora.calcularScoreTMA(unidades, unidades.get(2), RiskClassification.YELLOW);
        assertTrue(score > scoreUnidade1, "Unidade com menor TMA deve ter score maior");
        assertTrue(score > scoreUnidade2, "Unidade com menor TMA deve ter score maior");
    }

    @Test
    void deveCalcularScoreOcupacaoCorretamente() {
        UnidadeSaudeContext unidade = unidades.get(0); // Menor ocupação (0.3)
        
        double score = calculadora.calcularScoreOcupacao(unidades, unidade);
        
        // Com ocupação=0.3 e maxOcupacao=0.9, score = 1.0 - (0.3/0.9) ≈ 0.67
        assertTrue(score > 0.5, "Score deve ser > 0.5, mas foi " + score);
        assertTrue(score <= 1.0);
        // Verificar que a unidade com menor ocupação tem score maior que as outras
        double scoreUnidade1 = calculadora.calcularScoreOcupacao(unidades, unidades.get(1));
        double scoreUnidade2 = calculadora.calcularScoreOcupacao(unidades, unidades.get(2));
        assertTrue(score > scoreUnidade1, "Unidade com menor ocupação deve ter score maior");
        assertTrue(score > scoreUnidade2, "Unidade com menor ocupação deve ter score maior");
    }

    @Test
    void deveRetornarScoreEspecialidadeUmQuandoPossui() {
        UnidadeSaudeContext unidade = criarUnidadeComEspecialidade("Cardiologia");
        
        double score = calculadora.calcularScoreEspecialidade(unidade, "Cardiologia");
        
        assertEquals(1.0, score);
    }

    @Test
    void deveRetornarScoreEspecialidadeZeroQuandoNaoPossui() {
        UnidadeSaudeContext unidade = criarUnidadeComEspecialidade("Pediatria");
        
        double score = calculadora.calcularScoreEspecialidade(unidade, "Cardiologia");
        
        assertEquals(0.0, score);
    }

    private UnidadeSaudeContext criarUnidade(double distancia, int tma, double ocupacao) {
        // Criar indicadores com valores para todos os níveis do protocolo Manchester
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            tma,           // RED
            tma + 5,       // ORANGE
            tma + 10,      // YELLOW
            tma + 20,      // GREEN
            tma + 30,      // BLUE
            (int)(ocupacao * 10), 0, 10
        );
        
        return new UnidadeSaudeContext(
            UUID.randomUUID(),
            "Unidade Teste",
            -23.5505,
            -46.6333,
            List.of(),
            indicadores,
            distancia
        );
    }

    private UnidadeSaudeContext criarUnidadeComEspecialidade(String especialidade) {
        return new UnidadeSaudeContext(
            UUID.randomUUID(),
            "Unidade Teste",
            -23.5505,
            -46.6333,
            List.of(especialidade),
            IndicadoresOperacionais.padrao(),
            5.0
        );
    }
}
