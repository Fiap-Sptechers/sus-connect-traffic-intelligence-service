package com.fiap.sus.traffic.infrastructure.mapper;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;
import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper para converter UnitAnalyticsDTO (formato do LiveOps) em IndicadoresDTO (formato interno).
 */
@Component
@Slf4j
public class LiveOpsAnalyticsMapper {

    /**
     * Converte UnitAnalyticsDTO do LiveOps para IndicadoresDTO usado internamente.
     * 
     * @param analytics DTO do LiveOps Service
     * @return IndicadoresDTO com dados mapeados
     */
    public IndicadoresDTO toIndicadoresDTO(UnitAnalyticsDTO analytics) {
        if (analytics == null) {
            log.warn("UnitAnalyticsDTO é nulo, retornando indicadores padrão");
            return criarIndicadoresPadrao(null);
        }

        // Converter List<RiskAttendancePerformanceDTO> → Map<RiskClassification, Integer>
        Map<RiskClassification, Integer> tmaPorRisco = new HashMap<>();
        
        if (analytics.riskPerformance() != null && !analytics.riskPerformance().isEmpty()) {
            for (UnitAnalyticsDTO.RiskAttendancePerformanceDTO perf : analytics.riskPerformance()) {
                try {
                    RiskClassification risco = RiskClassification.valueOf(perf.risk().toUpperCase());
                    tmaPorRisco.put(risco, (int) perf.averageWaitTimeMinutes());
                } catch (IllegalArgumentException e) {
                    log.warn("Classificação de risco desconhecida: {}. Pulando.", perf.risk());
                }
            }
        }

        for (RiskClassification risco : RiskClassification.values()) {
            tmaPorRisco.putIfAbsent(risco, risco.getSlaMinutes());
        }

        long ocupacaoAtual = analytics.queueSnapshot() != null 
            ? analytics.queueSnapshot().totalPatients() 
            : 0;
        
        long pacientesEmEspera = analytics.queueSnapshot() != null 
            ? analytics.queueSnapshot().waitingCount() 
            : 0;

        int capacidadeNominal = 20; // TODO: Buscar do Network Service ou config

        UUID unidadeId;
        try {
            unidadeId = UUID.fromString(analytics.healthUnitId());
        } catch (IllegalArgumentException e) {
            log.error("ID da unidade inválido: {}. Usando UUID aleatório.", analytics.healthUnitId(), e);
            unidadeId = UUID.randomUUID();
        }

        return new IndicadoresDTO(
            unidadeId,
            tmaPorRisco,
            (int) ocupacaoAtual,
            (int) pacientesEmEspera,
            capacidadeNominal
        );
    }

    /**
     * Cria IndicadoresDTO com valores padrão conservadores.
     */
    public IndicadoresDTO criarIndicadoresPadrao(UUID unidadeId) {
        Map<RiskClassification, Integer> tmaPadrao = Map.of(
            RiskClassification.RED, 5,
            RiskClassification.ORANGE, 10,
            RiskClassification.YELLOW, 60,
            RiskClassification.GREEN, 120,
            RiskClassification.BLUE, 240
        );

        return new IndicadoresDTO(
            unidadeId != null ? unidadeId : UUID.randomUUID(),
            tmaPadrao,
            0,
            0,
            20
        );
    }
}
