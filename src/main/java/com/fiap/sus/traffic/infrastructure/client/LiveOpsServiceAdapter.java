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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import feign.FeignException;

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
        
        try {
            // Buscar analytics do LiveOps (retorna UnitAnalyticsDTO)
            UnitAnalyticsDTO analytics = client.buscarIndicadores(unidadeId.toString());
            
            log.debug("Resposta do LiveOps Service recebida para unidade {}: analytics={}", unidadeId, analytics != null ? "não-nulo" : "nulo");
            
            // Converter UnitAnalyticsDTO → IndicadoresDTO
            IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);
            
            // Garantir que nunca retornamos null
            if (indicadores == null) {
                log.warn("Mapper retornou null para unidade {}. Usando indicadores padrão.", unidadeId);
                indicadores = criarIndicadoresPadrao(unidadeId);
            }
            
            // Verificar se tmaPorRisco está null (não deveria acontecer, mas vamos garantir)
            if (indicadores.tmaPorRisco() == null) {
                log.error("IndicadoresDTO com tmaPorRisco null para unidade {}. Isso não deveria acontecer! Usando indicadores padrão.", unidadeId);
                indicadores = criarIndicadoresPadrao(unidadeId);
            }
            
            if (indicadores != null) {
                long ttl = properties.getCache().getTtlIndicadores().getSeconds();
                cachePort.putIndicadores(unidadeId, indicadores, ttl);
                log.debug("Indicadores salvos no cache para unidade {} com TTL de {}s", unidadeId, ttl);
            }
            
            return indicadores;
            
        } catch (HttpClientErrorException e) {
            log.warn("Erro 4xx ao buscar indicadores do LiveOps Service para unidade {}: Status {} - {}. Retornando indicadores padrão.", 
                unidadeId, e.getStatusCode(), e.getMessage());
            // Retornar indicadores padrão em caso de erro do cliente
            return criarIndicadoresPadrao(unidadeId);
        } catch (HttpServerErrorException e) {
            log.warn("Erro 5xx ao buscar indicadores do LiveOps Service para unidade {}: Status {} - {}. Retornando indicadores padrão.", 
                unidadeId, e.getStatusCode(), e.getMessage());
            // Retornar indicadores padrão em caso de erro do servidor
            return criarIndicadoresPadrao(unidadeId);
        } catch (FeignException e) {
            log.warn("Erro Feign ao buscar indicadores do LiveOps Service para unidade {}: Status {} - {}. Retornando indicadores padrão.", 
                unidadeId, e.status(), e.getMessage());
            // Retornar indicadores padrão em caso de erro do Feign
            return criarIndicadoresPadrao(unidadeId);
        } catch (Exception e) {
            log.warn("Erro inesperado ao buscar indicadores do LiveOps Service para unidade {}: {}. Retornando indicadores padrão.", 
                unidadeId, e.getMessage());
            // Retornar indicadores padrão em caso de erro inesperado
            return criarIndicadoresPadrao(unidadeId);
        }
    }
    
    /**
     * Cria IndicadoresDTO com valores padrão quando o LiveOps Service está indisponível.
     * Usa o método do mapper que já tem a lógica de valores padrão.
     */
    private IndicadoresDTO criarIndicadoresPadrao(UUID unidadeId) {
        return mapper.criarIndicadoresPadrao(unidadeId);
    }
}
