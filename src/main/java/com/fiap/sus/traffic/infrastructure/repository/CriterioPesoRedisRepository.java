package com.fiap.sus.traffic.infrastructure.repository;

import com.fiap.sus.traffic.domain.model.CriterioPeso;
import com.fiap.sus.traffic.domain.repository.CriterioPesoRepository;
import com.fiap.sus.traffic.infrastructure.cache.CacheKeyGenerator;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CriterioPesoRedisRepository implements CriterioPesoRepository {

    private final com.fiap.sus.traffic.application.port.CachePort cachePort;
    private final TrafficIntelligenceProperties properties;

    @Override
    public Optional<CriterioPeso> buscar() {
        String key = CacheKeyGenerator.pesosKey();
        Optional<CriterioPeso> pesos = cachePort.get(key, CriterioPeso.class);
        
        if (pesos.isEmpty()) {
            // Retornar valores padrão do application.yml
            var pesosPadrao = properties.getAlgoritmo().getPesos();
            CriterioPeso defaultPesos = CriterioPeso.builder()
                .pesoDistancia(pesosPadrao.getDistancia())
                .pesoTMA(pesosPadrao.getTma())
                .pesoOcupacao(pesosPadrao.getOcupacao())
                .pesoEspecialidade(pesosPadrao.getEspecialidade())
                .build();
            
            log.debug("Usando pesos padrão do configuration");
            return Optional.of(defaultPesos);
        }
        
        log.debug("Pesos recuperados do cache");
        return pesos;
    }

    @Override
    public void salvar(CriterioPeso pesos) {
        String key = CacheKeyGenerator.pesosKey();
        long ttl = properties.getCache().getTtlPesos().getSeconds();
        cachePort.put(key, pesos, ttl);
        log.info("Pesos salvos no cache com TTL de {} segundos", ttl);
    }
}
