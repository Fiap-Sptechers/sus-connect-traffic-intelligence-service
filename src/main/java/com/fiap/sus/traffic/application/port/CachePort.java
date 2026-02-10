package com.fiap.sus.traffic.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CachePort {
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, long ttlSeconds);
    void evict(String key);
    void evictPattern(String pattern);
    
    // Métodos específicos para facilitar uso
    <T> Optional<T> getIndicadores(UUID unidadeId, Class<T> type);
    <T> void putIndicadores(UUID unidadeId, T value, long ttlSeconds);
    
    <T> Optional<List<T>> getUnidades(String key, Class<T> type);
    <T> void putUnidades(String key, List<T> value, long ttlSeconds);
    
    // Métodos para cache de sugestões
    <T> Optional<List<T>> getSugestoes(String key, Class<T> type);
    <T> void putSugestoes(String key, List<T> value, long ttlSeconds);
}
