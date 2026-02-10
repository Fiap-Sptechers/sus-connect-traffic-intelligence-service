package com.fiap.sus.traffic.infrastructure.client;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.PageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
    name = "networkService",
    url = "${traffic.intelligence.network-service.url}",
    fallback = NetworkServiceClientFallback.class
)
public interface NetworkServiceClient {

    /**
     * Busca unidades de saúde próximas usando endereço como referência.
     * O Network Service faz geocodificação do endereço e busca unidades dentro do raio especificado.
     * 
     * O Network Service retorna uma Page, então recebemos como PageResponseDTO e extraímos o content.
     * 
     * @param baseAddress Endereço de referência (ex: "Av. Paulista, 1000, São Paulo, SP")
     * @param radius Raio de busca
     * @param distanceUnit Unidade de distância (KM, METERS, MILES)
     * @return Page contendo lista de unidades de saúde próximas com distância calculada
     */
    @GetMapping("/units/nearby")
    PageResponseDTO<HealthUnitResponseDTO> buscarUnidadesProximas(
        @RequestParam("baseAddress") String baseAddress,
        @RequestParam(value = "radius", required = false, defaultValue = "50.0") Double radius,
        @RequestParam(value = "distanceUnit", required = false, defaultValue = "KM") String distanceUnit,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "1000") int size
    );

    @GetMapping("/units/{id}")
    UnidadeSaudeDTO buscarUnidadePorId(@PathVariable UUID id);
}
