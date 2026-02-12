package com.fiap.sus.traffic.domain.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IndicadoresOperacionaisTest {

    @Test
    void deveCriarComValoresPadrao() {
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            null, null, null, null, null, null, null, null
        );

        assertEquals(5, indicadores.tmaRed());
        assertEquals(10, indicadores.tmaOrange());
        assertEquals(60, indicadores.tmaYellow());
        assertEquals(120, indicadores.tmaGreen());
        assertEquals(240, indicadores.tmaBlue());
        assertEquals(0, indicadores.ocupacaoAtual());
        assertEquals(0, indicadores.pacientesEmEspera());
        assertEquals(1, indicadores.capacidadeNominal());
    }

    @Test
    void deveRetornarTmaPorRisco() {
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            5, 10, 60, 120, 240, 0, 0, 10
        );

        assertEquals(5, indicadores.getTmaPorRisco(RiskClassification.RED));
        assertEquals(10, indicadores.getTmaPorRisco(RiskClassification.ORANGE));
        assertEquals(60, indicadores.getTmaPorRisco(RiskClassification.YELLOW));
        assertEquals(120, indicadores.getTmaPorRisco(RiskClassification.GREEN));
        assertEquals(240, indicadores.getTmaPorRisco(RiskClassification.BLUE));
    }

    @Test
    void deveCalcularTaxaOcupacao() {
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            5, 10, 60, 120, 240, 5, 0, 10
        );

        assertEquals(0.5, indicadores.calcularTaxaOcupacao(), 0.01);
    }

    @Test
    void deveRetornarTaxaOcupacaoUmQuandoCapacidadeZero() {
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            5, 10, 60, 120, 240, 5, 0, 0
        );

        assertEquals(1.0, indicadores.calcularTaxaOcupacao(), 0.01);
    }

    @Test
    void deveLimitarTaxaOcupacaoAteUm() {
        IndicadoresOperacionais indicadores = new IndicadoresOperacionais(
            5, 10, 60, 120, 240, 15, 0, 10
        );

        assertEquals(1.0, indicadores.calcularTaxaOcupacao(), 0.01);
    }

    @Test
    void deveCriarPesosPadrao() {
        IndicadoresOperacionais indicadores = IndicadoresOperacionais.padrao();

        assertNotNull(indicadores);
        assertEquals(5, indicadores.tmaRed());
        assertEquals(10, indicadores.tmaOrange());
        assertEquals(60, indicadores.tmaYellow());
        assertEquals(120, indicadores.tmaGreen());
        assertEquals(240, indicadores.tmaBlue());
        assertEquals(0, indicadores.ocupacaoAtual());
        assertEquals(0, indicadores.pacientesEmEspera());
        assertEquals(10, indicadores.capacidadeNominal());
    }

    @Test
    void deveCriarFromMapComValores() {
        Map<RiskClassification, Integer> tmaPorRisco = new HashMap<>();
        tmaPorRisco.put(RiskClassification.RED, 3);
        tmaPorRisco.put(RiskClassification.ORANGE, 8);
        tmaPorRisco.put(RiskClassification.YELLOW, 50);
        tmaPorRisco.put(RiskClassification.GREEN, 100);
        tmaPorRisco.put(RiskClassification.BLUE, 200);

        IndicadoresOperacionais indicadores = IndicadoresOperacionais.fromMap(
            tmaPorRisco, 5, 2, 20
        );

        assertEquals(3, indicadores.tmaRed());
        assertEquals(8, indicadores.tmaOrange());
        assertEquals(50, indicadores.tmaYellow());
        assertEquals(100, indicadores.tmaGreen());
        assertEquals(200, indicadores.tmaBlue());
        assertEquals(5, indicadores.ocupacaoAtual());
        assertEquals(2, indicadores.pacientesEmEspera());
        assertEquals(20, indicadores.capacidadeNominal());
    }

    @Test
    void deveCriarFromMapComMapNull() {
        IndicadoresOperacionais indicadores = IndicadoresOperacionais.fromMap(
            null, 5, 2, 20
        );

        assertEquals(5, indicadores.tmaRed());
        assertEquals(10, indicadores.tmaOrange());
        assertEquals(60, indicadores.tmaYellow());
        assertEquals(120, indicadores.tmaGreen());
        assertEquals(240, indicadores.tmaBlue());
        assertEquals(5, indicadores.ocupacaoAtual());
        assertEquals(2, indicadores.pacientesEmEspera());
        assertEquals(20, indicadores.capacidadeNominal());
    }

    @Test
    void deveCriarFromMapComValoresNulos() {
        Map<RiskClassification, Integer> tmaPorRisco = new HashMap<>();
        tmaPorRisco.put(RiskClassification.RED, 3);

        IndicadoresOperacionais indicadores = IndicadoresOperacionais.fromMap(
            tmaPorRisco, null, null, null
        );

        assertEquals(3, indicadores.tmaRed());
        assertEquals(10, indicadores.tmaOrange()); // padrão
        assertEquals(0, indicadores.ocupacaoAtual());
        assertEquals(0, indicadores.pacientesEmEspera());
        assertEquals(10, indicadores.capacidadeNominal()); // padrão
    }
}
