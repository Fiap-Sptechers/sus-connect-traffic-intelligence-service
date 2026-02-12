package com.fiap.sus.traffic.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisCacheAdapter cacheAdapter;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void deveRetornarEmptyQuandoChaveNaoExiste() {
        when(valueOperations.get("key")).thenReturn(null);

        Optional<String> result = cacheAdapter.get("key", String.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void deveRetornarValorQuandoChaveExiste() {
        String value = "test-value";
        when(valueOperations.get("key")).thenReturn(value);
        when(objectMapper.convertValue(value, String.class)).thenReturn(value);

        Optional<String> result = cacheAdapter.get("key", String.class);

        assertTrue(result.isPresent());
        assertEquals("test-value", result.get());
    }

    @Test
    void deveRetornarEmptyQuandoErroAoBuscar() {
        when(valueOperations.get("key")).thenThrow(new RuntimeException("Redis error"));

        Optional<String> result = cacheAdapter.get("key", String.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void deveArmazenarValorNoCache() {
        cacheAdapter.put("key", "value", 60);

        verify(valueOperations).set(eq("key"), eq("value"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void deveLidarComErroAoArmazenar() {
        doThrow(new RuntimeException("Redis error"))
            .when(valueOperations).set(anyString(), any(), any(Duration.class));

        assertDoesNotThrow(() -> cacheAdapter.put("key", "value", 60));
    }

    @Test
    void deveRemoverChaveDoCache() {
        when(redisTemplate.delete("key")).thenReturn(true);

        cacheAdapter.evict("key");

        verify(redisTemplate).delete("key");
    }

    @Test
    void deveLidarComErroAoRemover() {
        doThrow(new RuntimeException("Redis error"))
            .when(redisTemplate).delete(anyString());

        assertDoesNotThrow(() -> cacheAdapter.evict("key"));
    }

    @Test
    void deveRemoverPadraoDoCache() {
        Set<String> keys = Set.of("key1", "key2");
        when(redisTemplate.keys("pattern:*")).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        cacheAdapter.evictPattern("pattern:*");

        verify(redisTemplate).keys("pattern:*");
        verify(redisTemplate).delete(keys);
    }

    @Test
    void deveLidarComErroAoRemoverPadrao() {
        when(redisTemplate.keys("pattern:*")).thenThrow(new RuntimeException("Redis error"));

        assertDoesNotThrow(() -> cacheAdapter.evictPattern("pattern:*"));
    }

    @Test
    void deveBuscarIndicadores() {
        UUID unidadeId = UUID.randomUUID();
        Object value = new Object();
        when(valueOperations.get(anyString())).thenReturn(value);
        when(objectMapper.convertValue(value, Object.class)).thenReturn(value);

        Optional<Object> result = cacheAdapter.getIndicadores(unidadeId, Object.class);

        assertTrue(result.isPresent());
        verify(valueOperations).get(contains("indicadores"));
    }

    @Test
    void deveArmazenarIndicadores() {
        UUID unidadeId = UUID.randomUUID();
        Object value = new Object();

        cacheAdapter.putIndicadores(unidadeId, value, 60);

        verify(valueOperations).set(contains("indicadores"), eq(value), eq(Duration.ofSeconds(60)));
    }

    @Test
    void deveBuscarListaDeUnidades() {
        List<String> value = List.of("item1", "item2");
        when(valueOperations.get("key")).thenReturn(value);
        com.fasterxml.jackson.databind.type.TypeFactory typeFactory = 
            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        com.fasterxml.jackson.databind.type.CollectionType collectionType = 
            typeFactory.constructCollectionType(List.class, String.class);
        when(objectMapper.convertValue(any(), eq(collectionType))).thenReturn(value);

        Optional<List<String>> result = cacheAdapter.getUnidades("key", String.class);

        // Verifica que tentou buscar
        verify(valueOperations).get("key");
    }

    @Test
    void deveArmazenarListaDeUnidades() {
        List<String> value = List.of("item1", "item2");

        cacheAdapter.putUnidades("key", value, 60);

        verify(valueOperations).set(eq("key"), eq(value), eq(Duration.ofSeconds(60)));
    }

    @Test
    void deveBuscarSugestoes() {
        List<String> value = List.of("item1");
        when(valueOperations.get("key")).thenReturn(value);

        Optional<List<String>> result = cacheAdapter.getSugestoes("key", String.class);

        verify(valueOperations).get("key");
    }

    @Test
    void deveArmazenarSugestoes() {
        List<String> value = List.of("item1");

        cacheAdapter.putSugestoes("key", value, 60);

        verify(valueOperations).set(eq("key"), eq(value), eq(Duration.ofSeconds(60)));
    }
}
