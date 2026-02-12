package com.fiap.sus.traffic.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SugestaoOrdenadaTest {

    private final UUID unidadeId = UUID.randomUUID();

    @Test
    void deveCriarSugestaoValida() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            unidadeId,
            "Hospital Teste",
            0.85,
            5.0,
            30,
            "Próxima (5.0 km). TMA rápido (10 min)."
        );

        assertEquals(unidadeId, sugestao.unidadeId());
        assertEquals("Hospital Teste", sugestao.nome());
        assertEquals(0.85, sugestao.scoreFinal());
        assertEquals(5.0, sugestao.distanciaKm());
        assertEquals(30, sugestao.tempoEstimadoMinutos());
        assertEquals("Próxima (5.0 km). TMA rápido (10 min).", sugestao.razao());
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                null,
                "Hospital Teste",
                0.85,
                5.0,
                30,
                "Razão"
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                unidadeId,
                null,
                0.85,
                5.0,
                30,
                "Razão"
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                unidadeId,
                "   ",
                0.85,
                5.0,
                30,
                "Razão"
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoScoreFinalMenorQueZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                unidadeId,
                "Hospital Teste",
                -0.1,
                5.0,
                30,
                "Razão"
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoScoreFinalMaiorQueUm() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                unidadeId,
                "Hospital Teste",
                1.1,
                5.0,
                30,
                "Razão"
            );
        });
    }

    @Test
    void deveAceitarScoreFinalZero() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            unidadeId,
            "Hospital Teste",
            0.0,
            5.0,
            30,
            "Razão"
        );

        assertEquals(0.0, sugestao.scoreFinal());
    }

    @Test
    void deveAceitarScoreFinalUm() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            unidadeId,
            "Hospital Teste",
            1.0,
            5.0,
            30,
            "Razão"
        );

        assertEquals(1.0, sugestao.scoreFinal());
    }

    @Test
    void deveLancarExcecaoQuandoDistanciaKmNegativa() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                unidadeId,
                "Hospital Teste",
                0.85,
                -1.0,
                30,
                "Razão"
            );
        });
    }

    @Test
    void deveAceitarDistanciaKmZero() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            unidadeId,
            "Hospital Teste",
            0.85,
            0.0,
            30,
            "Razão"
        );

        assertEquals(0.0, sugestao.distanciaKm());
    }

    @Test
    void deveLancarExcecaoQuandoTempoEstimadoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SugestaoOrdenada(
                unidadeId,
                "Hospital Teste",
                0.85,
                5.0,
                -1,
                "Razão"
            );
        });
    }

    @Test
    void deveAceitarTempoEstimadoZero() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            unidadeId,
            "Hospital Teste",
            0.85,
            5.0,
            0,
            "Razão"
        );

        assertEquals(0, sugestao.tempoEstimadoMinutos());
    }

    @Test
    void deveInicializarRazaoVaziaQuandoNula() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            unidadeId,
            "Hospital Teste",
            0.85,
            5.0,
            30,
            null
        );

        assertEquals("", sugestao.razao());
    }
}
