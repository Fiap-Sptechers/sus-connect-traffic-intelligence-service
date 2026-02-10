package com.fiap.sus.traffic.application.usecase;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;
import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.application.port.LiveOpsServicePort;
import com.fiap.sus.traffic.application.port.NetworkServicePort;
import com.fiap.sus.traffic.core.exception.BusinessException;
import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.domain.model.*;
import com.fiap.sus.traffic.domain.repository.CriterioPesoRepository;
import com.fiap.sus.traffic.domain.service.AlgoritmoDirecionamentoService;
import com.fiap.sus.traffic.infrastructure.cache.CacheKeyGenerator;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import com.fiap.sus.traffic.shared.util.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarDirecionamentoUseCase {

    private final NetworkServicePort networkServicePort;
    private final LiveOpsServicePort liveOpsServicePort;
    private final CriterioPesoRepository pesosRepository;
    private final AlgoritmoDirecionamentoService algoritmoService;
    private final TrafficIntelligenceProperties properties;
    private final CachePort cachePort;

    public List<SugestaoOrdenada> executar(String baseAddress,
                                          RiskClassification riskClassification,
                                          String especialidade,
                                          Double radius,
                                          String distanceUnit) {
        
        long inicio = System.currentTimeMillis();
        log.info("Iniciando consulta de direcionamento: address={}, risco={}, especialidade={}, radius={}, unit={}",
            baseAddress, riskClassification, especialidade, radius, distanceUnit);

        // Validar e definir valores padrão
        if (baseAddress == null || baseAddress.isBlank()) {
            throw new ValidationException("baseAddress", "Endereço de referência é obrigatório");
        }
        
        if (radius == null) {
            radius = properties.getAlgoritmo().getRaioDefaultKm();
        }
        
        if (distanceUnit == null || distanceUnit.isBlank()) {
            distanceUnit = "KM";
        }
        
        validarParametros(radius);

        // Verificar cache de sugestões primeiro
        String cacheKey = CacheKeyGenerator.sugestoesKey(
            baseAddress, 
            riskClassification != null ? riskClassification.name() : null,
            especialidade,
            radius,
            distanceUnit
        );
        
        log.debug("Verificando cache com chave: {}", cacheKey);
        var cachedSugestoes = cachePort.getSugestoes(cacheKey, SugestaoOrdenada.class);
        if (cachedSugestoes.isPresent()) {
            long duracao = System.currentTimeMillis() - inicio;
            log.info("✅ Sugestões recuperadas do cache em {}ms. {} sugestões (chave: {})", 
                duracao, cachedSugestoes.get().size(), cacheKey);
            return cachedSugestoes.get();
        }

        log.info("❌ Cache miss para sugestões. Chave: {}. Processando consulta...", cacheKey);

        // Buscar unidades próximas
        List<UnidadeSaudeDTO> unidadesDTO = networkServicePort.buscarUnidadesProximas(
            baseAddress, radius, distanceUnit
        );

        if (unidadesDTO.isEmpty()) {
            log.warn("Nenhuma unidade encontrada no raio de {} {}", radius, distanceUnit);
            throw new BusinessException("NO_UNITS_FOUND", 
                String.format("Nenhuma unidade encontrada no raio de %.1f %s. Tente aumentar o raio.", radius, distanceUnit));
        }

        log.debug("Encontradas {} unidades no raio", unidadesDTO.size());

        // Construir contexto das unidades com indicadores
        List<UnidadeSaudeContext> unidadesContext = construirContextoUnidades(
            unidadesDTO, riskClassification
        );

        // Buscar pesos configurados
        CriterioPeso pesos = pesosRepository.buscar()
            .orElse(CriterioPeso.padrao());

        // Aplicar algoritmo de direcionamento
        List<SugestaoOrdenada> sugestoes = algoritmoService.calcularSugestoes(
            unidadesContext,
            pesos,
            riskClassification,
            especialidade,
            properties.getAlgoritmo().getMaxSugestoes()
        );

        // Armazenar no cache
        long ttl = properties.getCache().getTtlSugestoes().getSeconds();
        cachePort.putSugestoes(cacheKey, sugestoes, ttl);
        log.info("💾 Sugestões armazenadas no cache com chave: {} (TTL: {}s)", cacheKey, ttl);

        long duracao = System.currentTimeMillis() - inicio;
        log.info("Consulta concluída em {}ms. {} sugestões geradas", duracao, sugestoes.size());

        return sugestoes;
    }

    private void validarParametros(double radius) {
        double raioMin = properties.getAlgoritmo().getRaioMinimoKm();
        double raioMax = properties.getAlgoritmo().getRaioMaximoKm();
        
        if (radius < raioMin || radius > raioMax) {
            throw new ValidationException("radius", 
                String.format("Raio deve estar entre %.1f e %.1f km", raioMin, raioMax));
        }
    }

    private List<UnidadeSaudeContext> construirContextoUnidades(
            List<UnidadeSaudeDTO> unidadesDTO,
            RiskClassification riskClassification) {
        
        List<UnidadeSaudeContext> contextos = new ArrayList<>();

        for (UnidadeSaudeDTO dto : unidadesDTO) {
            // Validar que distance foi fornecida pelo Network Service
            if (dto.distance() == null || dto.distance().isBlank()) {
                log.warn("Unidade {} sem distância calculada. Pulando.", dto.id());
                continue;
            }

            // Converter String formatada ("1.5 km" ou "500 m") para Double (km)
            double distanciaKm;
            try {
                distanciaKm = DistanceUtils.parseDistanceToKm(dto.distance());
            } catch (IllegalArgumentException e) {
                log.error("Erro ao converter distância da unidade {}: {}", dto.id(), dto.distance(), e);
                continue; // Pular unidade com distância inválida
            }

            // Buscar indicadores operacionais
            IndicadoresDTO indicadoresDTO = liveOpsServicePort.buscarIndicadores(dto.id());
            
            IndicadoresOperacionais indicadores = IndicadoresOperacionais.fromMap(
                indicadoresDTO != null ? indicadoresDTO.tmaPorRisco() : null,
                indicadoresDTO != null ? indicadoresDTO.ocupacaoAtual() : null,
                indicadoresDTO != null ? indicadoresDTO.pacientesEmEspera() : null,
                indicadoresDTO != null ? indicadoresDTO.capacidadeNominal() : null
            );

            // Extrair especialidades (assumindo que vem do Network Service)
            List<String> especialidades = new ArrayList<>();
            // TODO: Adicionar lógica para extrair especialidades do DTO quando disponível

            UnidadeSaudeContext contexto = new UnidadeSaudeContext(
                dto.id(),
                dto.name(),
                null,  // latitude opcional (não é mais necessária)
                null,  // longitude opcional (não é mais necessária)
                especialidades,
                indicadores,
                distanciaKm  // Distância convertida de String para Double
            );

            contextos.add(contexto);
        }

        return contextos;
    }
}
