package com.fiap.sus.traffic.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceUtilsParseTest {

    @Test
    void deveConverterKmParaDouble() {
        assertEquals(1.5, DistanceUtils.parseDistanceToKm("1.5 km"), 0.01);
        assertEquals(10.0, DistanceUtils.parseDistanceToKm("10 km"), 0.01);
        assertEquals(0.5, DistanceUtils.parseDistanceToKm("0.5 km"), 0.01);
    }

    @Test
    void deveConverterMetrosParaKm() {
        assertEquals(0.5, DistanceUtils.parseDistanceToKm("500 m"), 0.01);
        assertEquals(1.0, DistanceUtils.parseDistanceToKm("1000 m"), 0.01);
        assertEquals(0.1, DistanceUtils.parseDistanceToKm("100 m"), 0.01);
    }

    @Test
    void deveConverterNumeroDireto() {
        assertEquals(5.0, DistanceUtils.parseDistanceToKm("5.0"), 0.01);
        assertEquals(10.5, DistanceUtils.parseDistanceToKm("10.5"), 0.01);
    }

    @Test
    void deveLancarExcecaoQuandoStringNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceUtils.parseDistanceToKm(null);
        });
    }

    @Test
    void deveLancarExcecaoQuandoStringVazia() {
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceUtils.parseDistanceToKm("");
        });
    }

    @Test
    void deveLancarExcecaoQuandoStringEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceUtils.parseDistanceToKm("   ");
        });
    }

    @Test
    void deveLancarExcecaoQuandoFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceUtils.parseDistanceToKm("invalid");
        });
    }

    @Test
    void deveLidarComEspacos() {
        assertEquals(1.5, DistanceUtils.parseDistanceToKm("  1.5 km  "), 0.01);
        assertEquals(0.5, DistanceUtils.parseDistanceToKm("  500 m  "), 0.01);
    }
}
