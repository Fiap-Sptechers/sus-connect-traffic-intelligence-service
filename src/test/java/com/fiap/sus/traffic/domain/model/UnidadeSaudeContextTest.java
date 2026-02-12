package com.fiap.sus.traffic.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UnidadeSaudeContextTest {

    private final UUID unidadeId = UUID.randomUUID();
    private final IndicadoresOperacionais indicadores = IndicadoresOperacionais.padrao();

    @Test
    void deveCriarUnidadeValida() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            -23.5505,
            -46.6333,
            List.of("Cardiologia"),
            indicadores,
            5.0
        );

        assertEquals(unidadeId, unidade.unidadeId());
        assertEquals("Hospital Teste", unidade.nome());
        assertEquals(-23.5505, unidade.latitude());
        assertEquals(-46.6333, unidade.longitude());
        assertEquals(1, unidade.especialidades().size());
        assertEquals(5.0, unidade.distanciaKm());
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                null,
                "Hospital Teste",
                -23.5505,
                -46.6333,
                List.of(),
                indicadores,
                5.0
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                unidadeId,
                null,
                -23.5505,
                -46.6333,
                List.of(),
                indicadores,
                5.0
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                unidadeId,
                "   ",
                -23.5505,
                -46.6333,
                List.of(),
                indicadores,
                5.0
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoLatitudeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                unidadeId,
                "Hospital Teste",
                91.0,
                -46.6333,
                List.of(),
                indicadores,
                5.0
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoLongitudeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                unidadeId,
                "Hospital Teste",
                -23.5505,
                181.0,
                List.of(),
                indicadores,
                5.0
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoDistanciaKmNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                unidadeId,
                "Hospital Teste",
                -23.5505,
                -46.6333,
                List.of(),
                indicadores,
                null
            );
        });
    }

    @Test
    void deveLancarExcecaoQuandoDistanciaKmNegativa() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnidadeSaudeContext(
                unidadeId,
                "Hospital Teste",
                -23.5505,
                -46.6333,
                List.of(),
                indicadores,
                -1.0
            );
        });
    }

    @Test
    void deveAceitarLatitudeLongitudeNulas() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            null,
            null,
            List.of(),
            indicadores,
            5.0
        );

        assertNull(unidade.latitude());
        assertNull(unidade.longitude());
    }

    @Test
    void deveInicializarEspecialidadesVaziasQuandoNulas() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            null,
            null,
            null,
            indicadores,
            5.0
        );

        assertNotNull(unidade.especialidades());
        assertTrue(unidade.especialidades().isEmpty());
    }

    @Test
    void deveInicializarIndicadoresPadraoQuandoNulos() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            null,
            null,
            List.of(),
            null,
            5.0
        );

        assertNotNull(unidade.indicadores());
        assertEquals(5, unidade.indicadores().tmaRed());
    }

    @Test
    void deveRetornarTrueQuandoPossuiEspecialidade() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            null,
            null,
            List.of("Cardiologia", "Pediatria"),
            indicadores,
            5.0
        );

        assertTrue(unidade.possuiEspecialidade("Cardiologia"));
        assertTrue(unidade.possuiEspecialidade("CARDIOLOGIA")); // case insensitive
        assertTrue(unidade.possuiEspecialidade("Pediatria"));
        assertFalse(unidade.possuiEspecialidade("Ortopedia"));
    }

    @Test
    void deveRetornarTrueQuandoEspecialidadeNulaOuVazia() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            null,
            null,
            List.of("Cardiologia"),
            indicadores,
            5.0
        );

        assertTrue(unidade.possuiEspecialidade(null));
        assertTrue(unidade.possuiEspecialidade(""));
        assertTrue(unidade.possuiEspecialidade("   "));
    }

    @Test
    void deveRetornarTrueQuandoListaEspecialidadesVazia() {
        UnidadeSaudeContext unidade = new UnidadeSaudeContext(
            unidadeId,
            "Hospital Teste",
            null,
            null,
            List.of(),
            indicadores,
            5.0
        );

        assertTrue(unidade.possuiEspecialidade("Cardiologia"));
    }
}
