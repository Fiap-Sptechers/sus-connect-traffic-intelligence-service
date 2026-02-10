package com.fiap.sus.traffic.infrastructure.cache;

import com.fiap.sus.traffic.application.port.CachePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss para chave: {}", key);
                return Optional.empty();
            }
            
            log.debug("✅ Cache hit para chave: {}", key);
            T result = objectMapper.convertValue(value, type);
            return Optional.of(result);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar do cache: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> void put(String key, T value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            log.debug("Valor armazenado no cache: {} (TTL: {}s)", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Erro ao armazenar no cache: {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Chave removida do cache: {}", key);
        } catch (Exception e) {
            log.error("Erro ao remover do cache: {}", key, e);
        }
    }

    @Override
    public void evictPattern(String pattern) {
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.debug("Padrão removido do cache: {}", pattern);
        } catch (Exception e) {
            log.error("Erro ao remover padrão do cache: {}", pattern, e);
        }
    }

    @Override
    public <T> Optional<T> getIndicadores(UUID unidadeId, Class<T> type) {
        String key = CacheKeyGenerator.indicadoresKey(unidadeId);
        return get(key, type);
    }

    @Override
    public <T> void putIndicadores(UUID unidadeId, T value, long ttlSeconds) {
        String key = CacheKeyGenerator.indicadoresKey(unidadeId);
        put(key, value, ttlSeconds);
    }

    @Override
    public <T> Optional<List<T>> getUnidades(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss para lista (chave: {})", key);
                return Optional.empty();
            }
            
            log.debug("✅ Cache hit para lista (chave: {})", key);
            List<T> result = objectMapper.convertValue(value, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, type));
            return Optional.of(result);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar lista do cache: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> void putUnidades(String key, List<T> value, long ttlSeconds) {
        put(key, value, ttlSeconds);
    }

    @Override
    public <T> Optional<List<T>> getSugestoes(String key, Class<T> type) {
        return getUnidades(key, type); // Reutiliza a mesma lógica
    }

    @Override
    public <T> void putSugestoes(String key, List<T> value, long ttlSeconds) {
        putUnidades(key, value, ttlSeconds); // Reutiliza a mesma lógica
    }
}
