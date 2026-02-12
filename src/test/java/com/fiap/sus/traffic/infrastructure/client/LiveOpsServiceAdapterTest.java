package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;
import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import com.fiap.sus.traffic.infrastructure.mapper.LiveOpsAnalyticsMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LiveOpsServiceAdapterTest {

    @Mock
    private LiveOpsServiceClient client;

    @Mock
    private CachePort cachePort;

    @Mock
    private TrafficIntelligenceProperties properties;

    @Mock
    private LiveOpsAnalyticsMapper mapper;

    @InjectMocks
    private LiveOpsServiceAdapter adapter;

    private UUID unidadeId;

    @BeforeEach
    void setUp() {
        unidadeId = UUID.randomUUID();
        var cacheProperties = new TrafficIntelligenceProperties.Cache();
        cacheProperties.setTtlIndicadores(Duration.ofSeconds(30));
        lenient().when(properties.getCache()).thenReturn(cacheProperties);
    }

    @Test
    void deveRetornarIndicadoresDoCache() {
        IndicadoresDTO cached = new IndicadoresDTO(
            unidadeId,
            Map.of(RiskClassification.RED, 5),
            10,
            5,
            20
        );
        when(cachePort.getIndicadores(eq(unidadeId), eq(IndicadoresDTO.class)))
            .thenReturn(Optional.of(cached));

        IndicadoresDTO result = adapter.buscarIndicadores(unidadeId);

        assertEquals(cached, result);
        verify(client, never()).buscarIndicadores(anyString());
    }

    @Test
    void deveBuscarIndicadoresDoLiveOpsService() {
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(10L, 5L, 5L),
            List.of()
        );
        IndicadoresDTO indicadores = new IndicadoresDTO(
            unidadeId,
            Map.of(RiskClassification.RED, 5),
            10,
            5,
            20
        );

        when(cachePort.getIndicadores(eq(unidadeId), eq(IndicadoresDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarIndicadores(unidadeId.toString())).thenReturn(analytics);
        when(mapper.toIndicadoresDTO(analytics)).thenReturn(indicadores);

        IndicadoresDTO result = adapter.buscarIndicadores(unidadeId);

        assertNotNull(result);
        verify(cachePort).putIndicadores(eq(unidadeId), eq(indicadores), eq(30L));
    }

    @Test
    void deveRetornarIndicadoresPadraoQuandoHttpClientErrorException() {
        IndicadoresDTO padrao = new IndicadoresDTO(
            unidadeId,
            Map.of(RiskClassification.RED, 5),
            0,
            0,
            20
        );

        when(cachePort.getIndicadores(eq(unidadeId), eq(IndicadoresDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarIndicadores(unidadeId.toString()))
            .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND, "Not Found"));
        when(mapper.criarIndicadoresPadrao(unidadeId)).thenReturn(padrao);

        IndicadoresDTO result = adapter.buscarIndicadores(unidadeId);

        assertNotNull(result);
        assertEquals(padrao, result);
    }

    @Test
    void deveRetornarIndicadoresPadraoQuandoHttpServerErrorException() {
        IndicadoresDTO padrao = new IndicadoresDTO(
            unidadeId,
            Map.of(RiskClassification.RED, 5),
            0,
            0,
            20
        );

        when(cachePort.getIndicadores(eq(unidadeId), eq(IndicadoresDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarIndicadores(unidadeId.toString()))
            .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));
        when(mapper.criarIndicadoresPadrao(unidadeId)).thenReturn(padrao);

        IndicadoresDTO result = adapter.buscarIndicadores(unidadeId);

        assertNotNull(result);
        assertEquals(padrao, result);
    }

    @Test
    void deveRetornarIndicadoresPadraoQuandoFeignException() {
        IndicadoresDTO padrao = new IndicadoresDTO(
            unidadeId,
            Map.of(RiskClassification.RED, 5),
            0,
            0,
            20
        );

        when(cachePort.getIndicadores(eq(unidadeId), eq(IndicadoresDTO.class)))
            .thenReturn(Optional.empty());
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(502);
        when(feignException.getMessage()).thenReturn("Bad Gateway");
        when(client.buscarIndicadores(unidadeId.toString())).thenThrow(feignException);
        when(mapper.criarIndicadoresPadrao(unidadeId)).thenReturn(padrao);

        IndicadoresDTO result = adapter.buscarIndicadores(unidadeId);

        assertNotNull(result);
        assertEquals(padrao, result);
    }

    @Test
    void deveRetornarIndicadoresPadraoQuandoMapperRetornaNull() {
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            null,
            null
        );
        IndicadoresDTO padrao = new IndicadoresDTO(
            unidadeId,
            Map.of(RiskClassification.RED, 5),
            0,
            0,
            20
        );

        when(cachePort.getIndicadores(eq(unidadeId), eq(IndicadoresDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarIndicadores(unidadeId.toString())).thenReturn(analytics);
        when(mapper.toIndicadoresDTO(analytics)).thenReturn(null);
        when(mapper.criarIndicadoresPadrao(unidadeId)).thenReturn(padrao);

        IndicadoresDTO result = adapter.buscarIndicadores(unidadeId);

        assertNotNull(result);
        assertEquals(padrao, result);
    }
}
