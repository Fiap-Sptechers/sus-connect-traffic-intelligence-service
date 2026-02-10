package com.fiap.sus.traffic.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private static final String NETWORK_SERVICE_HEALTH_URL = "http://localhost:8080/actuator/health";
    private static final String LIVEOPS_SERVICE_HEALTH_URL = "http://localhost:8081/actuator/health";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        boolean allServicesUp = true;
        
        // Verificar Network Service via health endpoint
        String networkStatus = checkServiceHealth(NETWORK_SERVICE_HEALTH_URL, "Network Service");
        builder.withDetail("network-service", networkStatus);
        if (!networkStatus.contains("UP")) {
            allServicesUp = false;
        }
        
        // Verificar LiveOps Service via health endpoint
        String liveopsStatus = checkServiceHealth(LIVEOPS_SERVICE_HEALTH_URL, "LiveOps Service");
        builder.withDetail("liveops-service", liveopsStatus);
        if (!liveopsStatus.contains("UP")) {
            allServicesUp = false;
        }
        
        // Não marcar o serviço como DOWN se apenas um serviço externo estiver indisponível
        // O Traffic Intelligence pode funcionar parcialmente mesmo se um serviço externo estiver down
        if (!allServicesUp) {
            builder.withDetail("warning", "Alguns serviços externos estão indisponíveis");
            // Manter como UP, mas com warning nos detalhes
        }
        
        return builder.build();
    }

    private String checkServiceHealth(String url, String serviceName) {
        try {
            Instant start = Instant.now();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Duration duration = Duration.between(start, Instant.now());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("{} health check: UP ({}ms)", serviceName, duration.toMillis());
                return String.format("UP (%dms)", duration.toMillis());
            } else {
                log.warn("{} health check: DOWN - Status {}", serviceName, response.getStatusCode());
                return String.format("DOWN - Status %s", response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.warn("{} health check: DOWN - {}", serviceName, e.getMessage());
            return String.format("DOWN - %s", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao verificar health de {}: {}", serviceName, e.getMessage(), e);
            return String.format("ERROR - %s", e.getMessage());
        }
    }
}
