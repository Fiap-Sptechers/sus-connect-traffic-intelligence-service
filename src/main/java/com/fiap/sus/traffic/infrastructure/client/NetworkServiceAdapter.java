package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.application.port.NetworkServicePort;
import com.fiap.sus.traffic.core.exception.ExternalServiceException;
import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.infrastructure.cache.CacheKeyGenerator;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.PageResponseDTO;
import com.fiap.sus.traffic.infrastructure.mapper.NetworkServiceMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetworkServiceAdapter implements NetworkServicePort {

    private final NetworkServiceClient client;
    private final CachePort cachePort;
    private final TrafficIntelligenceProperties properties;
    private final NetworkServiceMapper mapper;

    @Override
    @CircuitBreaker(name = "networkService")
    @Retry(name = "networkService")
    public List<UnidadeSaudeDTO> buscarUnidadesProximas(String baseAddress, Double radius, String distanceUnit) {
        if (baseAddress == null || baseAddress.isBlank()) {
            throw new ValidationException("baseAddress", "Endereço de referência não pode ser nulo ou vazio");
        }
        
        if (radius == null) {
            radius = 50.0; // Valor padrão
        }
        
        if (distanceUnit == null || distanceUnit.isBlank()) {
            distanceUnit = "KM"; // Valor padrão
        }
        
        String cacheKey = CacheKeyGenerator.unidadesKey(baseAddress, radius, distanceUnit);
        
        Optional<List<UnidadeSaudeDTO>> cached = cachePort.getUnidades(cacheKey, UnidadeSaudeDTO.class);
        if (cached.isPresent()) {
            log.debug("Unidades recuperadas do cache para endereço: {}", baseAddress);
            return cached.get();
        }

        log.debug("Buscando unidades próximas: address={}, radius={}, unit={}", baseAddress, radius, distanceUnit);
        
        try {
            // Network Service retorna Page<HealthUnitResponseDTO>, precisamos extrair o content
            PageResponseDTO<HealthUnitResponseDTO> pageResponse = client.buscarUnidadesProximas(
                baseAddress, radius, distanceUnit, 0, 1000 // Buscar todas as unidades (até 1000)
            );
            
            // Converter HealthUnitResponseDTO para UnidadeSaudeDTO
            List<UnidadeSaudeDTO> unidades = mapper.toUnidadeSaudeDTOList(
                pageResponse != null ? pageResponse.getContent() : List.of()
            );
            
            log.debug("Encontradas {} unidades do Network Service", unidades.size());
            
            long ttl = properties.getCache().getTtlUnidades().getSeconds();
            cachePort.putUnidades(cacheKey, unidades, ttl);
            
            return unidades;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro ao buscar unidades do Network Service: Status {}", e.getStatusCode(), e);
            throw new ExternalServiceException("Network Service", e.getMessage(), e.getStatusCode().value());
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar unidades do Network Service", e);
            throw new ExternalServiceException("Network Service", "Erro ao buscar unidades próximas", e);
        }
    }

    @Override
    @CircuitBreaker(name = "networkService")
    @Retry(name = "networkService")
    public UnidadeSaudeDTO buscarUnidadePorId(UUID id) {
        log.debug("Buscando unidade por ID: {}", id);
        return client.buscarUnidadePorId(id);
    }
}
