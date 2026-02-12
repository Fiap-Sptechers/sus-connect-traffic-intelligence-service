package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.core.exception.ExternalServiceException;
import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.PageResponseDTO;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import com.fiap.sus.traffic.infrastructure.mapper.NetworkServiceMapper;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class NetworkServiceAdapterTest {

    @Mock
    private NetworkServiceClient client;

    @Mock
    private CachePort cachePort;

    @Mock
    private TrafficIntelligenceProperties properties;

    @Mock
    private NetworkServiceMapper mapper;

    @InjectMocks
    private NetworkServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        var cacheProperties = new TrafficIntelligenceProperties.Cache();
        cacheProperties.setTtlUnidades(Duration.ofSeconds(60));
        lenient().when(properties.getCache()).thenReturn(cacheProperties);
    }

    @Test
    void deveLancarExcecaoQuandoBaseAddressNulo() {
        assertThrows(ValidationException.class, () -> {
            adapter.buscarUnidadesProximas(null, 10.0, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoBaseAddressVazio() {
        assertThrows(ValidationException.class, () -> {
            adapter.buscarUnidadesProximas("   ", 10.0, "KM");
        });
    }

    @Test
    void deveUsarRadiusPadraoQuandoNulo() {
        String address = "Rua Teste, 123";
        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarUnidadesProximas(eq(address), eq(50.0), eq("KM"), eq(0), eq(1000)))
            .thenReturn(new PageResponseDTO<>(List.of(), 0, 0, 1000, 0, true, true, 0));
        when(mapper.toUnidadeSaudeDTOList(anyList())).thenReturn(List.of());

        adapter.buscarUnidadesProximas(address, null, "KM");

        verify(client).buscarUnidadesProximas(eq(address), eq(50.0), eq("KM"), eq(0), eq(1000));
    }

    @Test
    void deveUsarDistanceUnitPadraoQuandoNulo() {
        String address = "Rua Teste, 123";
        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarUnidadesProximas(eq(address), eq(10.0), eq("KM"), eq(0), eq(1000)))
            .thenReturn(new PageResponseDTO<>(List.of(), 0, 0, 1000, 0, true, true, 0));
        when(mapper.toUnidadeSaudeDTOList(anyList())).thenReturn(List.of());

        adapter.buscarUnidadesProximas(address, 10.0, null);

        verify(client).buscarUnidadesProximas(eq(address), eq(10.0), eq("KM"), eq(0), eq(1000));
    }

    @Test
    void deveRetornarUnidadesDoCache() {
        String address = "Rua Teste, 123";
        List<UnidadeSaudeDTO> cached = List.of(
            new UnidadeSaudeDTO(UUID.randomUUID(), "Hospital 1", "123", null, List.of(), "5.0 KM")
        );
        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.of(cached));

        List<UnidadeSaudeDTO> result = adapter.buscarUnidadesProximas(address, 10.0, "KM");

        assertEquals(cached, result);
        verify(client, never()).buscarUnidadesProximas(anyString(), anyDouble(), anyString(), anyInt(), anyInt());
    }

    @Test
    void deveBuscarUnidadesDoNetworkService() {
        String address = "Rua Teste, 123";
        HealthUnitResponseDTO response = new HealthUnitResponseDTO(
            UUID.randomUUID(), "Hospital 1", "123", null, null, "5.0 KM"
        );
        UnidadeSaudeDTO dto = new UnidadeSaudeDTO(
            UUID.randomUUID(), "Hospital 1", "123", null, List.of(), "5.0 KM"
        );

        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarUnidadesProximas(anyString(), anyDouble(), anyString(), anyInt(), anyInt()))
            .thenReturn(new PageResponseDTO<>(List.of(response), 1, 1, 1000, 0, true, true, 1));
        when(mapper.toUnidadeSaudeDTOList(anyList())).thenReturn(List.of(dto));

        List<UnidadeSaudeDTO> result = adapter.buscarUnidadesProximas(address, 10.0, "KM");

        assertNotNull(result);
        verify(cachePort).putUnidades(anyString(), anyList(), eq(60L));
    }

    @Test
    void deveLancarExcecaoQuandoHttpClientErrorException() {
        String address = "Rua Teste, 123";
        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarUnidadesProximas(anyString(), anyDouble(), anyString(), anyInt(), anyInt()))
            .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST, "Bad Request"));

        assertThrows(ExternalServiceException.class, () -> {
            adapter.buscarUnidadesProximas(address, 10.0, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoHttpServerErrorException() {
        String address = "Rua Teste, 123";
        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.empty());
        when(client.buscarUnidadesProximas(anyString(), anyDouble(), anyString(), anyInt(), anyInt()))
            .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"));

        assertThrows(ExternalServiceException.class, () -> {
            adapter.buscarUnidadesProximas(address, 10.0, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoFeignException() {
        String address = "Rua Teste, 123";
        when(cachePort.getUnidades(anyString(), eq(UnidadeSaudeDTO.class)))
            .thenReturn(Optional.empty());
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(502);
        when(feignException.getMessage()).thenReturn("Bad Gateway");
        when(client.buscarUnidadesProximas(anyString(), anyDouble(), anyString(), anyInt(), anyInt()))
            .thenThrow(feignException);

        assertThrows(ExternalServiceException.class, () -> {
            adapter.buscarUnidadesProximas(address, 10.0, "KM");
        });
    }

    @Test
    void deveBuscarUnidadePorId() {
        UUID id = UUID.randomUUID();
        UnidadeSaudeDTO dto = new UnidadeSaudeDTO(id, "Hospital", "123", null, List.of(), "5.0 KM");
        when(client.buscarUnidadePorId(id)).thenReturn(dto);

        UnidadeSaudeDTO result = adapter.buscarUnidadePorId(id);

        assertEquals(dto, result);
        verify(client).buscarUnidadePorId(id);
    }
}
