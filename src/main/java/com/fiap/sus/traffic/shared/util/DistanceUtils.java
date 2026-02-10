package com.fiap.sus.traffic.shared.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcula a distância entre dois pontos geográficos usando a Fórmula de Haversine
     * @param lat1 Latitude do primeiro ponto
     * @param lon1 Longitude do primeiro ponto
     * @param lat2 Latitude do segundo ponto
     * @param lon2 Longitude do segundo ponto
     * @return Distância em quilômetros
     */
    public static double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Converte a distância formatada (String) para quilômetros (Double)
     * Exemplos: "1.5 km" -> 1.5, "500 m" -> 0.5
     * 
     * @param distanceString String formatada: "1.5 km" ou "500 m"
     * @return Distância em quilômetros
     * @throws IllegalArgumentException se o formato for inválido
     */
    public static double parseDistanceToKm(String distanceString) {
        if (distanceString == null || distanceString.isBlank()) {
            throw new IllegalArgumentException("Distance string não pode ser nula ou vazia");
        }

        try {
            String trimmed = distanceString.trim();
            
            if (trimmed.endsWith("km")) {
                // Formato: "1.5 km"
                String value = trimmed.replace("km", "").trim();
                return Double.parseDouble(value);
            } else if (trimmed.endsWith("m")) {
                // Formato: "500 m"
                String value = trimmed.replace("m", "").trim();
                double metros = Double.parseDouble(value);
                return metros / 1000.0; // Converter metros para km
            } else {
                // Tentar parse direto (assumindo que já está em km)
                return Double.parseDouble(trimmed);
            }
        } catch (NumberFormatException e) {
            log.error("Erro ao converter distância: {}", distanceString, e);
            throw new IllegalArgumentException(
                String.format("Formato de distância inválido: %s. Esperado: '1.5 km' ou '500 m'", distanceString),
                e
            );
        }
    }
}
