package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "liveOpsService",
    url = "${traffic.intelligence.liveops-service.url}",
    fallback = LiveOpsServiceClientFallback.class
)
public interface LiveOpsServiceClient {

    /**
     * Busca analytics avançados de uma unidade de saúde.
     * Endpoint: GET /analytics/units/{id}/advanced
     * 
     * @param healthUnitId ID da unidade de saúde (String)
     * @return UnitAnalyticsDTO com indicadores operacionais
     */
    @GetMapping("/analytics/units/{id}/advanced")
    UnitAnalyticsDTO buscarIndicadores(@PathVariable("id") String healthUnitId);
}
