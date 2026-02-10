package com.fiap.sus.traffic.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceUtilsTest {

    @Test
    void deveCalcularDistanciaEntreDoisPontos() {
        // São Paulo
        double lat1 = -23.5505;
        double lon1 = -46.6333;
        
        // Rio de Janeiro
        double lat2 = -22.9068;
        double lon2 = -43.1729;
        
        double distancia = DistanceUtils.calcularDistanciaKm(lat1, lon1, lat2, lon2);
        
        // Distância aproximada entre SP e RJ é ~350km
        assertTrue(distancia > 300 && distancia < 400);
    }

    @Test
    void deveRetornarZeroParaMesmoPonto() {
        double lat = -23.5505;
        double lon = -46.6333;
        
        double distancia = DistanceUtils.calcularDistanciaKm(lat, lon, lat, lon);
        
        assertEquals(0.0, distancia, 0.1);
    }

    @Test
    void deveCalcularDistanciaPequena() {
        // Dois pontos próximos em São Paulo
        double lat1 = -23.5505;
        double lon1 = -46.6333;
        double lat2 = -23.5510;
        double lon2 = -46.6340;
        
        double distancia = DistanceUtils.calcularDistanciaKm(lat1, lon1, lat2, lon2);
        
        assertTrue(distancia > 0);
        assertTrue(distancia < 1.0); // Menos de 1km
    }
}
