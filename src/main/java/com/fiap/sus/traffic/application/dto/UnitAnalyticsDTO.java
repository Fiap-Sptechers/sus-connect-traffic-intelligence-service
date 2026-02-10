package com.fiap.sus.traffic.application.dto;

import java.util.List;

/**
 * DTO para receber a resposta do LiveOps Service - Analytics endpoint.
 * Representa os dados de analytics avançados de uma unidade de saúde.
 */
public record UnitAnalyticsDTO(
    String healthUnitId,
    long generalAverageWaitTimeMinutes,
    LiveQueueSnapshotDTO queueSnapshot,
    List<RiskAttendancePerformanceDTO> riskPerformance
) {
    /**
     * Snapshot da fila de atendimento em tempo real.
     */
    public record LiveQueueSnapshotDTO(
        long totalPatients,
        long waitingCount,
        long inProgressCount
    ) {}

    /**
     * Performance de atendimento por classificação de risco (Protocolo Manchester).
     */
    public record RiskAttendancePerformanceDTO(
        String risk,  // RED, ORANGE, YELLOW, GREEN, BLUE
        long averageWaitTimeMinutes,
        int maxWaitTimeLimit,
        boolean isSlaBreached
    ) {}
}
