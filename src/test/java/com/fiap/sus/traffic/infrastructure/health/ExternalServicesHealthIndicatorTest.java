package com.fiap.sus.traffic.infrastructure.health;

import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalServicesHealthIndicatorTest {

    @Mock
    private TrafficIntelligenceProperties properties;

    @Mock
    private RestTemplate restTemplate;

    private ExternalServicesHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new ExternalServicesHealthIndicator(properties);
        // restTemplate é criado internamente, então precisamos usar reflection para substituir
        ReflectionTestUtils.setField(healthIndicator, "restTemplate", restTemplate);

        var networkService = new TrafficIntelligenceProperties.NetworkService();
        networkService.setUrl("https://network-service.example.com");
        var liveopsService = new TrafficIntelligenceProperties.LiveOpsService();
        liveopsService.setUrl("https://liveops-service.example.com");

        when(properties.getNetworkService()).thenReturn(networkService);
        when(properties.getLiveopsService()).thenReturn(liveopsService);
    }

    @Test
    void deveRetornarHealthUpQuandoServicosDisponiveis() {
        when(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("network-service"));
        assertTrue(health.getDetails().containsKey("liveops-service"));
    }

    @Test
    void deveRetornarHealthUpQuandoNetworkServiceIndisponivel() {
        when(restTemplate.getForEntity(contains("network"), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));
        when(restTemplate.getForEntity(contains("liveops"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("Indisponível", health.getDetails().get("network-service"));
    }

    @Test
    void deveRetornarHealthUpQuandoLiveOpsServiceIndisponivel() {
        when(restTemplate.getForEntity(contains("network"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));
        when(restTemplate.getForEntity(contains("liveops"), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("Indisponível", health.getDetails().get("liveops-service"));
    }

    @Test
    void deveRetornarHealthUpQuandoAmbosServicosIndisponiveis() {
        when(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("Indisponível", health.getDetails().get("network-service"));
        assertEquals("Indisponível", health.getDetails().get("liveops-service"));
    }

    @Test
    void deveRetornarHealthUpQuandoNetworkServiceRetornaErro() {
        when(restTemplate.getForEntity(contains("network"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));
        when(restTemplate.getForEntity(contains("liveops"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().get("network-service").toString().contains("Status"));
    }

    @Test
    void deveIgnorarLocalhost() {
        var networkService = new TrafficIntelligenceProperties.NetworkService();
        networkService.setUrl("http://localhost:8080");
        var liveopsService = new TrafficIntelligenceProperties.LiveOpsService();
        liveopsService.setUrl("http://localhost:8081");

        when(properties.getNetworkService()).thenReturn(networkService);
        when(properties.getLiveopsService()).thenReturn(liveopsService);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("URL não configurada ou localhost (ignorado)", health.getDetails().get("network-service"));
        assertEquals("URL não configurada ou localhost (ignorado)", health.getDetails().get("liveops-service"));
        verify(restTemplate, never()).getForEntity(any(String.class), eq(String.class));
    }

    @Test
    void deveIgnorarUrlNula() {
        var networkService = new TrafficIntelligenceProperties.NetworkService();
        networkService.setUrl(null);
        var liveopsService = new TrafficIntelligenceProperties.LiveOpsService();
        liveopsService.setUrl("");

        when(properties.getNetworkService()).thenReturn(networkService);
        when(properties.getLiveopsService()).thenReturn(liveopsService);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("URL não configurada ou localhost (ignorado)", health.getDetails().get("network-service"));
        assertEquals("URL não configurada ou localhost (ignorado)", health.getDetails().get("liveops-service"));
    }
}
