package com.fiap.sus.traffic.infrastructure.cache;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CacheKeyGeneratorTest {

    @Test
    void deveGerarChaveIndicadores() {
        UUID unidadeId = UUID.randomUUID();
        String key = CacheKeyGenerator.indicadoresKey(unidadeId);

        assertTrue(key.startsWith("traffic:intelligence:indicadores:"));
        assertTrue(key.contains(unidadeId.toString()));
    }

    @Test
    void deveGerarChaveUnidades() {
        String key = CacheKeyGenerator.unidadesKey("Rua Teste, 123", 10.0, "KM");

        assertTrue(key.startsWith("traffic:intelligence:unidades:"));
        assertTrue(key.contains("rua_teste,_123"));
        assertTrue(key.contains("10.0"));
        assertTrue(key.contains("KM"));
    }

    @Test
    void deveNormalizarEnderecoNaChaveUnidades() {
        String key1 = CacheKeyGenerator.unidadesKey("Rua Teste, 123", 10.0, "KM");
        String key2 = CacheKeyGenerator.unidadesKey("  Rua Teste, 123  ", 10.0, "KM");

        assertEquals(key1, key2);
    }

    @Test
    void deveArredondarRadiusNaChaveUnidades() {
        // Math.round(10.15 * 10) / 10.0 = 101.5 / 10.0 = 10.15... não, Math.round(10.15 * 10) = 102, então 102/10.0 = 10.2
        // Math.round(10.14 * 10) / 10.0 = 101.4 / 10.0 = 10.14... não, Math.round(10.14 * 10) = 101, então 101/10.0 = 10.1
        String key1 = CacheKeyGenerator.unidadesKey("Rua Teste", 10.15, "KM");
        String key2 = CacheKeyGenerator.unidadesKey("Rua Teste", 10.14, "KM");

        // 10.15 arredonda para 10.2, 10.14 arredonda para 10.1
        assertTrue(key1.contains("10.2"));
        assertTrue(key2.contains("10.1"));
    }

    @Test
    void deveUsarRadiusPadraoQuandoNulo() {
        String key = CacheKeyGenerator.unidadesKey("Rua Teste", null, "KM");

        assertTrue(key.contains("50.0"));
    }

    @Test
    void deveUsarDistanceUnitPadraoQuandoNulo() {
        String key = CacheKeyGenerator.unidadesKey("Rua Teste", 10.0, null);

        assertTrue(key.endsWith("KM"));
    }

    @Test
    void deveGerarChaveSugestoes() {
        String key = CacheKeyGenerator.sugestoesKey(
            "Rua Teste, 123", "RED", "Cardiologia", 10.0, "KM"
        );

        assertTrue(key.startsWith("traffic:intelligence:sugestoes:"));
        assertTrue(key.contains("rua_teste,_123"));
        assertTrue(key.contains("RED"));
        assertTrue(key.contains("cardiologia"));
        assertTrue(key.contains("10.0"));
        assertTrue(key.contains("KM"));
    }

    @Test
    void deveNormalizarEspecialidadeNaChaveSugestoes() {
        String key1 = CacheKeyGenerator.sugestoesKey(
            "Rua Teste", "RED", "Cardiologia", 10.0, "KM"
        );
        String key2 = CacheKeyGenerator.sugestoesKey(
            "Rua Teste", "RED", "  Cardiologia  ", 10.0, "KM"
        );

        // A normalização remove espaços e converte para minúsculas com underscores
        assertTrue(key1.contains("cardiologia"));
        assertTrue(key2.contains("cardiologia"));
    }

    @Test
    void deveUsarNoneQuandoRiskClassificationNulo() {
        String key = CacheKeyGenerator.sugestoesKey(
            "Rua Teste", null, "Cardiologia", 10.0, "KM"
        );

        assertTrue(key.contains("NONE"));
    }

    @Test
    void deveUsarNoneQuandoEspecialidadeNulaOuVazia() {
        String key1 = CacheKeyGenerator.sugestoesKey(
            "Rua Teste", "RED", null, 10.0, "KM"
        );
        String key2 = CacheKeyGenerator.sugestoesKey(
            "Rua Teste", "RED", "   ", 10.0, "KM"
        );

        assertTrue(key1.contains("none"));
        assertTrue(key2.contains("none"));
    }

    @Test
    void deveGerarChavePesos() {
        String key = CacheKeyGenerator.pesosKey();

        assertEquals("traffic:intelligence:pesos", key);
    }

    @Test
    void deveNormalizarEnderecoComEspacosMultiplos() {
        String key = CacheKeyGenerator.unidadesKey("Rua   Teste   123", 10.0, "KM");

        assertFalse(key.contains("   "));
        assertTrue(key.contains("rua_teste_123"));
    }

    @Test
    void deveNormalizarEnderecoVazio() {
        String key = CacheKeyGenerator.unidadesKey("", 10.0, "KM");

        assertTrue(key.contains("unknown"));
    }

    @Test
    void deveNormalizarEnderecoNulo() {
        String key = CacheKeyGenerator.unidadesKey(null, 10.0, "KM");

        assertTrue(key.contains("unknown"));
    }
}
