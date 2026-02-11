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
import feign.FeignException;

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

                String normalizedAddress = normalizeAddressForNetworkService(baseAddress);
        
        log.info("🔍 Buscando unidades próximas: address='{}' → normalizado='{}', radius={}, unit={}", 
            baseAddress, normalizedAddress, radius, distanceUnit);
        
        try {
            // Network Service retorna Page<HealthUnitResponseDTO>, precisamos extrair o content
            PageResponseDTO<HealthUnitResponseDTO> pageResponse = client.buscarUnidadesProximas(
                normalizedAddress, radius, distanceUnit, 0, 1000 // Buscar todas as unidades (até 1000)
            );
            
            // Converter HealthUnitResponseDTO para UnidadeSaudeDTO
            List<UnidadeSaudeDTO> unidades = mapper.toUnidadeSaudeDTOList(
                pageResponse != null ? pageResponse.getContent() : List.of()
            );
            
            log.debug("Encontradas {} unidades do Network Service", unidades.size());
            
            long ttl = properties.getCache().getTtlUnidades().getSeconds();
            cachePort.putUnidades(cacheKey, unidades, ttl);
            
            return unidades;
            
        } catch (HttpClientErrorException e) {
            log.error("Erro 4xx ao buscar unidades do Network Service: Status {} - {}", 
                e.getStatusCode(), e.getMessage(), e);
            throw new ExternalServiceException("Network Service", 
                String.format("Erro ao comunicar com Network Service (Status %d): %s", 
                    e.getStatusCode().value(), e.getMessage()), 
                e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("Erro 5xx ao buscar unidades do Network Service: Status {} - {}", 
                e.getStatusCode(), e.getMessage(), e);
            throw new ExternalServiceException("Network Service", 
                String.format("Erro ao comunicar com Network Service (Status %d): %s", 
                    e.getStatusCode().value(), e.getMessage()), 
                e.getStatusCode().value());
        } catch (FeignException e) {
            log.error("Erro Feign ao buscar unidades do Network Service: Status {} - {}", 
                e.status(), e.getMessage(), e);
            int statusCode = e.status() > 0 ? e.status() : 502;
            throw new ExternalServiceException("Network Service", 
                String.format("Erro ao comunicar com Network Service (Status %d): %s", 
                    statusCode, e.getMessage()), 
                statusCode);
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar unidades do Network Service: {}", e.getMessage(), e);
            throw new ExternalServiceException("Network Service", 
                "Erro ao buscar unidades próximas: " + e.getMessage(), e);
        }
    }

    @Override
    @CircuitBreaker(name = "networkService")
    @Retry(name = "networkService")
    public UnidadeSaudeDTO buscarUnidadePorId(UUID id) {
        log.debug("Buscando unidade por ID: {}", id);
        return client.buscarUnidadePorId(id);
    }

    /**
     * Normaliza o endereço antes de enviar ao Network Service.
     * Expande abreviações comuns que podem causar problemas na geocodificação.
     * 
     * @param address Endereço original
     * @return Endereço normalizado
     */
    private String normalizeAddressForNetworkService(String address) {
        if (address == null || address.isBlank()) {
            return address;
        }
        
        String normalized = address.trim();
        
        
        normalized = normalized.replaceAll("\\bAv\\.\\s*", "Avenida ");
        normalized = normalized.replaceAll("\\bAv\\s+", "Avenida ");
        normalized = normalized.replaceAll("\\bR\\.\\s*", "Rua ");
        normalized = normalized.replaceAll("\\bR\\s+", "Rua ");
        normalized = normalized.replaceAll("\\bRua\\s+R\\.", "Rua");
        normalized = normalized.replaceAll("\\bAvenida\\s+Av\\.", "Avenida");
        normalized = normalized.replaceAll("\\bPraça\\s+Pç\\.", "Praça");
        normalized = normalized.replaceAll("\\bPç\\.\\s*", "Praça ");
        normalized = normalized.replaceAll("\\bDr\\.\\s*", "Doutor ");
        normalized = normalized.replaceAll("\\bDr\\s+", "Doutor ");
        normalized = normalized.replaceAll("\\bProf\\.\\s*", "Professor ");
        normalized = normalized.replaceAll("\\bProf\\s+", "Professor ");
        
        
        normalized = normalized.replaceAll("\\s+", " ");
        
        normalized = normalized.trim();
        
        return normalized;
    }
}
