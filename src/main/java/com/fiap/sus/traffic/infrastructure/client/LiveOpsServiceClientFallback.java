package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LiveOpsServiceClientFallback implements LiveOpsServiceClient {

    @Override
    public UnitAnalyticsDTO buscarIndicadores(String healthUnitId) {
        log.warn("Fallback: LiveOps Service indisponível. Retornando analytics padrão conservadores para unidade {}", healthUnitId);
        
        // Retornar UnitAnalyticsDTO com valores padrão conservadores
        // Isso será convertido pelo mapper para IndicadoresDTO
        return new UnitAnalyticsDTO(
            healthUnitId,
            30L, // generalAverageWaitTimeMinutes padrão
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(0, 0, 0),
            java.util.List.of(
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("RED", 5, 0, false),
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("ORANGE", 10, 10, false),
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("YELLOW", 60, 60, false),
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("GREEN", 120, 120, false),
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("BLUE", 240, 240, false)
            )
        );
    }
}
