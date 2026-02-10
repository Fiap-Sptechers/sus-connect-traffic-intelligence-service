package com.fiap.sus.traffic.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CriterioPesoTest {

    @Test
    void deveCriarCriterioPesoValido() {
        CriterioPeso pesos = CriterioPeso.builder()
            .pesoDistancia(0.3)
            .pesoTMA(0.4)
            .pesoOcupacao(0.2)
            .pesoEspecialidade(0.1)
            .build();

        assertEquals(0.3, pesos.pesoDistancia());
        assertEquals(0.4, pesos.pesoTMA());
        assertEquals(0.2, pesos.pesoOcupacao());
        assertEquals(0.1, pesos.pesoEspecialidade());
    }

    @Test
    void deveLancarExcecaoQuandoSomaMaiorQueUm() {
        assertThrows(IllegalArgumentException.class, () -> {
            CriterioPeso.builder()
                .pesoDistancia(0.5)
                .pesoTMA(0.4)
                .pesoOcupacao(0.2)
                .pesoEspecialidade(0.1)
                .build();
        });
    }

    @Test
    void deveLancarExcecaoQuandoPesoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            CriterioPeso.builder()
                .pesoDistancia(-0.1)
                .pesoTMA(0.4)
                .pesoOcupacao(0.2)
                .pesoEspecialidade(0.1)
                .build();
        });
    }

    @Test
    void deveLancarExcecaoQuandoPesoMaiorQueUm() {
        assertThrows(IllegalArgumentException.class, () -> {
            CriterioPeso.builder()
                .pesoDistancia(1.1)
                .pesoTMA(0.4)
                .pesoOcupacao(0.2)
                .pesoEspecialidade(0.1)
                .build();
        });
    }

    @Test
    void deveCriarPesosPadrao() {
        CriterioPeso pesos = CriterioPeso.padrao();
        assertNotNull(pesos);
        assertEquals(0.3, pesos.pesoDistancia());
        assertEquals(0.4, pesos.pesoTMA());
    }
}
