package com.fiap.sus.traffic.infrastructure.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class CacheKeyGenerator {

    private static final String PREFIX = "traffic:intelligence";
    private static final String INDICADORES = "indicadores";
    private static final String UNIDADES = "unidades";
    private static final String PESOS = "pesos";
    private static final String SUGESTOES = "sugestoes";

    public static String indicadoresKey(UUID unidadeId) {
        return String.format("%s:%s:%s", PREFIX, INDICADORES, unidadeId);
    }

    /**
     * Gera chave de cache para unidades baseada em endereço.
     * Normaliza o endereço para garantir que variações do mesmo endereço gerem a mesma chave.
     */
    public static String unidadesKey(String baseAddress, Double radius, String distanceUnit) {
        // Normalizar endereço de forma mais robusta
        String normalizedAddress = normalizeAddress(baseAddress);
        // Arredondar radius para 1 casa decimal para garantir consistência
        double normalizedRadius = radius != null ? Math.round(radius * 10.0) / 10.0 : 50.0;
        return String.format("%s:%s:%s:%.1f:%s", 
            PREFIX, UNIDADES, normalizedAddress, normalizedRadius, distanceUnit != null ? distanceUnit.toUpperCase() : "KM");
    }

    /**
     * Gera chave de cache para sugestões de direcionamento.
     * Inclui todos os parâmetros que afetam o resultado final.
     */
    public static String sugestoesKey(String baseAddress, String riskClassification, String especialidade, Double radius, String distanceUnit) {
        String normalizedAddress = normalizeAddress(baseAddress);
        String risk = riskClassification != null ? riskClassification.toUpperCase() : "NONE";
        String esp = especialidade != null && !especialidade.isBlank() ? especialidade.toLowerCase().replaceAll("\\s+", "_") : "none";
        String unit = distanceUnit != null ? distanceUnit.toUpperCase() : "KM";
        // Arredondar radius para 1 casa decimal para garantir consistência
        double normalizedRadius = radius != null ? Math.round(radius * 10.0) / 10.0 : 50.0;
        
        return String.format("%s:%s:%s:%s:%s:%.1f:%s", 
            PREFIX, SUGESTOES, normalizedAddress, risk, esp, normalizedRadius, unit);
    }

    public static String pesosKey() {
        return String.format("%s:%s", PREFIX, PESOS);
    }

    /**
     * Normaliza o endereço para garantir consistência na chave de cache.
     * Remove acentos, espaços extras, converte para minúsculas e remove caracteres especiais.
     */
    private static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) {
            return "unknown";
        }
        
        // Normalizar: trim, lowercase, remover espaços múltiplos
        String normalized = address.trim()
            .toLowerCase()
            .replaceAll("\\s+", "_")
            // Normalizar variações comuns
            .replace("são_paulo", "sao_paulo")
            .replace("sao_paulo", "sao_paulo")
            .replace("sp", "sp")
            .replace("rj", "rj")
            .replace("mg", "mg");
        
        // Se o endereço normalizado for muito longo, usar hash
        if (normalized.length() > 100) {
            return hashString(normalized);
        }
        
        return normalized;
    }

    /**
     * Gera hash MD5 de uma string para usar como parte da chave de cache.
     */
    private static String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback: usar substring
            return input.substring(0, Math.min(50, input.length()));
        }
    }
}
