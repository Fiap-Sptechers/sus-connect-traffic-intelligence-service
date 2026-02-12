package com.fiap.sus.traffic.domain.service;

import com.fiap.sus.traffic.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlgoritmoDirecionamentoServiceTest {

    @Mock
    private CalculadoraScoreService calculadoraScore;

    @InjectMocks
    private AlgoritmoDirecionamentoService algoritmoService;

    private CriterioPeso pesos;
    private List<UnidadeSaudeContext> unidades;

    @BeforeEach
    void setUp() {
        pesos = CriterioPeso.builder()
            .pesoDistancia(0.3)
            .pesoTMA(0.4)
            .pesoOcupacao(0.2)
            .pesoEspecialidade(0.1)
            .build();

        unidades = List.of(
            criarUnidade("Cardiologia"),
            criarUnidade("Pediatria"),
            criarUnidade("Cardiologia")
        );
    }

    @Test
    void deveCalcularSugestoesComEspecialidade() {
        when(calculadoraScore.calcularScoreDistancia(anyList(), any())).thenReturn(0.8);
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(0.7);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(0.6);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidades, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertEquals(2, sugestoes.size());
        verify(calculadoraScore, times(2)).calcularScoreDistancia(anyList(), any());
        verify(calculadoraScore, times(2)).calcularScoreTMA(anyList(), any(), eq(RiskClassification.RED));
        verify(calculadoraScore, times(2)).calcularScoreOcupacao(anyList(), any());
        verify(calculadoraScore, times(2)).calcularScoreEspecialidade(any(), eq("Cardiologia"));
    }

    @Test
    void deveFiltrarUnidadesPorEspecialidade() {
        // Não precisa mockar os scores pois nenhuma unidade será processada
        // (todas serão filtradas por não terem a especialidade)

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidades, pesos, RiskClassification.RED, "Ortopedia", 5
        );

        assertEquals(0, sugestoes.size());
    }

    @Test
    void deveRetornarVazioQuandoNenhumaUnidadePossuiEspecialidade() {
        List<UnidadeSaudeContext> unidadesSemEspecialidade = List.of(
            criarUnidade("Pediatria"),
            criarUnidade("Neurologia")
        );

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidadesSemEspecialidade, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertEquals(0, sugestoes.size());
        verify(calculadoraScore, never()).calcularScoreDistancia(anyList(), any());
    }

    @Test
    void deveLimitarNumeroDeSugestoes() {
        List<UnidadeSaudeContext> muitasUnidades = List.of(
            criarUnidade("Cardiologia"),
            criarUnidade("Cardiologia"),
            criarUnidade("Cardiologia"),
            criarUnidade("Cardiologia"),
            criarUnidade("Cardiologia"),
            criarUnidade("Cardiologia")
        );

        when(calculadoraScore.calcularScoreDistancia(anyList(), any())).thenReturn(0.8);
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(0.7);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(0.6);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            muitasUnidades, pesos, RiskClassification.RED, "Cardiologia", 3
        );

        assertEquals(3, sugestoes.size());
    }

    @Test
    void deveOrdenarSugestoesPorScoreFinal() {
        when(calculadoraScore.calcularScoreDistancia(anyList(), any()))
            .thenReturn(0.8, 0.6); // Primeira unidade tem score maior
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(0.7);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(0.6);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<UnidadeSaudeContext> duasUnidades = List.of(
            criarUnidade("Cardiologia"),
            criarUnidade("Cardiologia")
        );

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            duasUnidades, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertEquals(2, sugestoes.size());
        assertTrue(sugestoes.get(0).scoreFinal() >= sugestoes.get(1).scoreFinal());
    }

    @Test
    void deveCalcularScoreFinalCorretamente() {
        when(calculadoraScore.calcularScoreDistancia(anyList(), any())).thenReturn(0.8);
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(0.7);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(0.6);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidades, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertFalse(sugestoes.isEmpty());
        SugestaoOrdenada primeira = sugestoes.get(0);
        
        // Score final = 0.3*0.8 + 0.4*0.7 + 0.2*0.6 + 0.1*1.0 = 0.24 + 0.28 + 0.12 + 0.1 = 0.74
        assertEquals(0.74, primeira.scoreFinal(), 0.01);
        assertTrue(primeira.scoreFinal() >= 0.0 && primeira.scoreFinal() <= 1.0);
    }

    @Test
    void deveNormalizarScoreFinalEntreZeroEUm() {
        // Simular scores que resultariam em valor > 1.0
        when(calculadoraScore.calcularScoreDistancia(anyList(), any())).thenReturn(1.0);
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(1.0);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(1.0);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidades, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertFalse(sugestoes.isEmpty());
        for (SugestaoOrdenada sugestao : sugestoes) {
            assertTrue(sugestao.scoreFinal() >= 0.0 && sugestao.scoreFinal() <= 1.0);
        }
    }

    @Test
    void deveCalcularTempoEstimado() {
        when(calculadoraScore.calcularScoreDistancia(anyList(), any())).thenReturn(0.8);
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(0.7);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(0.6);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidades, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertFalse(sugestoes.isEmpty());
        SugestaoOrdenada primeira = sugestoes.get(0);
        assertTrue(primeira.tempoEstimadoMinutos() >= 0);
    }

    @Test
    void deveIncluirRazaoNaSugestao() {
        when(calculadoraScore.calcularScoreDistancia(anyList(), any())).thenReturn(0.8);
        when(calculadoraScore.calcularScoreTMA(anyList(), any(), any())).thenReturn(0.7);
        when(calculadoraScore.calcularScoreOcupacao(anyList(), any())).thenReturn(0.6);
        when(calculadoraScore.calcularScoreEspecialidade(any(), eq("Cardiologia"))).thenReturn(1.0);

        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidades, pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertFalse(sugestoes.isEmpty());
        SugestaoOrdenada primeira = sugestoes.get(0);
        assertNotNull(primeira.razao());
    }

    @Test
    void deveRetornarVazioQuandoListaUnidadesVazia() {
        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            List.of(), pesos, RiskClassification.RED, "Cardiologia", 5
        );

        assertEquals(0, sugestoes.size());
        verify(calculadoraScore, never()).calcularScoreDistancia(anyList(), any());
    }

    private UnidadeSaudeContext criarUnidade(String especialidade) {
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            5, 10, 60, 120, 240, 5, 0, 10
        );
        return new UnidadeSaudeContext(
            UUID.randomUUID(),
            "Unidade Teste",
            null,
            null,
            List.of(especialidade),
            indicadores,
            5.0
        );
    }
}
