package com.fiap.sus.traffic.infrastructure.repository;

import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.domain.model.CriterioPeso;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CriterioPesoRedisRepositoryTest {

    @Mock
    private CachePort cachePort;

    @Mock
    private TrafficIntelligenceProperties properties;

    @InjectMocks
    private CriterioPesoRedisRepository repository;

    @BeforeEach
    void setUp() {
        var algoritmoProperties = new TrafficIntelligenceProperties.Algoritmo();
        var pesosProperties = new TrafficIntelligenceProperties.Algoritmo.Pesos();
        var cacheProperties = new TrafficIntelligenceProperties.Cache();
        
        pesosProperties.setDistancia(0.3);
        pesosProperties.setTma(0.4);
        pesosProperties.setOcupacao(0.2);
        pesosProperties.setEspecialidade(0.1);
        algoritmoProperties.setPesos(pesosProperties);
        cacheProperties.setTtlPesos(Duration.ofSeconds(3600));
        
        lenient().when(properties.getAlgoritmo()).thenReturn(algoritmoProperties);
        lenient().when(properties.getCache()).thenReturn(cacheProperties);
    }

    @Test
    void deveBuscarPesosDoCache() {
        CriterioPeso pesos = CriterioPeso.builder()
            .pesoDistancia(0.4)
            .pesoTMA(0.3)
            .pesoOcupacao(0.2)
            .pesoEspecialidade(0.1)
            .build();

        when(cachePort.get(anyString(), eq(CriterioPeso.class)))
            .thenReturn(Optional.of(pesos));

        Optional<CriterioPeso> result = repository.buscar();

        assertTrue(result.isPresent());
        assertEquals(0.4, result.get().pesoDistancia());
        assertEquals(0.3, result.get().pesoTMA());
        verify(cachePort).get(anyString(), eq(CriterioPeso.class));
    }

    @Test
    void deveRetornarPesosPadraoQuandoCacheVazio() {
        when(cachePort.get(anyString(), eq(CriterioPeso.class)))
            .thenReturn(Optional.empty());
        
        // Configurar pesos padrão diferentes para o teste
        var algoritmoProps = new TrafficIntelligenceProperties.Algoritmo();
        var pesosProps = new TrafficIntelligenceProperties.Algoritmo.Pesos();
        pesosProps.setDistancia(0.5);
        pesosProps.setTma(0.3);
        pesosProps.setOcupacao(0.1);
        pesosProps.setEspecialidade(0.1);
        algoritmoProps.setPesos(pesosProps);
        when(properties.getAlgoritmo()).thenReturn(algoritmoProps);

        Optional<CriterioPeso> result = repository.buscar();

        assertTrue(result.isPresent());
        assertEquals(0.5, result.get().pesoDistancia());
        assertEquals(0.3, result.get().pesoTMA());
        assertEquals(0.1, result.get().pesoOcupacao());
        assertEquals(0.1, result.get().pesoEspecialidade());
    }

    @Test
    void deveSalvarPesosNoCache() {
        CriterioPeso pesos = CriterioPeso.builder()
            .pesoDistancia(0.4)
            .pesoTMA(0.3)
            .pesoOcupacao(0.2)
            .pesoEspecialidade(0.1)
            .build();

        repository.salvar(pesos);

        verify(cachePort).put(anyString(), eq(pesos), eq(3600L));
    }
}
