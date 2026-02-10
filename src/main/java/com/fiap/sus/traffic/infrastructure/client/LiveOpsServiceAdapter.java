package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;
import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.application.port.LiveOpsServicePort;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import com.fiap.sus.traffic.infrastructure.mapper.LiveOpsAnalyticsMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiveOpsServiceAdapter implements LiveOpsServicePort {

    private final LiveOpsServiceClient client;
    private final CachePort cachePort;
    private final TrafficIntelligenceProperties properties;
    private final LiveOpsAnalyticsMapper mapper;

    @Override
    @CircuitBreaker(name = "liveOpsService")
    @Retry(name = "liveOpsService")
    public IndicadoresDTO buscarIndicadores(UUID unidadeId) {
        Optional<IndicadoresDTO> cached = cachePort.getIndicadores(unidadeId, IndicadoresDTO.class);
        if (cached.isPresent()) {
            log.debug("Indicadores recuperados do cache para unidade {}", unidadeId);
            return cached.get();
        }

        log.debug("Buscando indicadores para unidade {} do LiveOps Service", unidadeId);
        
        // Buscar analytics do LiveOps (retorna UnitAnalyticsDTO)
        UnitAnalyticsDTO analytics = client.buscarIndicadores(unidadeId.toString());
        
        // Converter UnitAnalyticsDTO → IndicadoresDTO
        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);
        
        if (indicadores != null) {
            long ttl = properties.getCache().getTtlIndicadores().getSeconds();
            cachePort.putIndicadores(unidadeId, indicadores, ttl);
            log.debug("Indicadores salvos no cache para unidade {} com TTL de {}s", unidadeId, ttl);
        }
        
        return indicadores;
    }
}
