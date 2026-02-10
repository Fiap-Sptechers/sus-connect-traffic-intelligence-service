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

    private final RestTemplate restTemplate = new RestTemplate();
    private final com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties properties;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // Construir URLs dos serviços externos a partir das propriedades
        String networkServiceUrl = properties.getNetworkService().getUrl();
        String liveopsServiceUrl = properties.getLiveopsService().getUrl();
        
        // Verificar Network Service via health endpoint (se URL estiver configurada)
        if (networkServiceUrl != null && !networkServiceUrl.isEmpty() && !networkServiceUrl.contains("localhost")) {
            String networkHealthUrl = networkServiceUrl.replaceAll("/$", "") + "/actuator/health";
            String networkStatus = checkServiceHealth(networkHealthUrl, "Network Service");
            builder.withDetail("network-service", networkStatus);
        } else {
            builder.withDetail("network-service", "URL não configurada ou localhost (ignorado)");
        }
        
        // Verificar LiveOps Service via health endpoint (se URL estiver configurada)
        if (liveopsServiceUrl != null && !liveopsServiceUrl.isEmpty() && !liveopsServiceUrl.contains("localhost")) {
            String liveopsHealthUrl = liveopsServiceUrl.replaceAll("/$", "") + "/actuator/health";
            String liveopsStatus = checkServiceHealth(liveopsHealthUrl, "LiveOps Service");
            builder.withDetail("liveops-service", liveopsStatus);
        } else {
            builder.withDetail("liveops-service", "URL não configurada ou localhost (ignorado)");
        }
        
        // Sempre retornar UP - health checks externos não devem bloquear o health check principal
        // O Traffic Intelligence pode funcionar parcialmente mesmo se serviços externos estiverem down
        return builder.build();
    }

    private String checkServiceHealth(String url, String serviceName) {
        try {
            Instant start = Instant.now();
            // Timeout curto para não bloquear o health check principal
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Duration duration = Duration.between(start, Instant.now());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("{} health check: UP ({}ms)", serviceName, duration.toMillis());
                return String.format("UP (%dms)", duration.toMillis());
            } else {
                log.warn("{} health check: Status {}", serviceName, response.getStatusCode());
                return String.format("Status %s", response.getStatusCode());
            }
        } catch (RestClientException e) {
            // Não logar como erro - apenas aviso, não bloqueia health check
            log.debug("{} health check não disponível: {}", serviceName, e.getMessage());
            return "Indisponível";
        } catch (Exception e) {
            log.debug("Erro ao verificar health de {}: {}", serviceName, e.getMessage());
            return "Erro ao verificar";
        }
    }
}
