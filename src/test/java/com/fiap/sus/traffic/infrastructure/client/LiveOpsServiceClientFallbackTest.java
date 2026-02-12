package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiveOpsServiceClientFallbackTest {

    private final LiveOpsServiceClientFallback fallback = new LiveOpsServiceClientFallback();

    @Test
    void deveRetornarUnitAnalyticsDTOComValoresPadrao() {
        String unitId = "unit-id";
        UnitAnalyticsDTO result = fallback.buscarIndicadores(unitId);

        assertNotNull(result);
        assertEquals(unitId, result.healthUnitId());
        assertEquals(30L, result.generalAverageWaitTimeMinutes());
        assertNotNull(result.queueSnapshot());
        assertEquals(0, result.queueSnapshot().totalPatients());
        assertNotNull(result.riskPerformance());
        assertEquals(5, result.riskPerformance().size());
    }
}
