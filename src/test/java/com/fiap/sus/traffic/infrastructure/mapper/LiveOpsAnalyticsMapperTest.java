package com.fiap.sus.traffic.infrastructure.mapper;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;
import com.fiap.sus.traffic.application.dto.UnitAnalyticsDTO;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LiveOpsAnalyticsMapperTest {

    private final LiveOpsAnalyticsMapper mapper = new LiveOpsAnalyticsMapper();

    @Test
    void deveConverterUnitAnalyticsDTOParaIndicadoresDTO() {
        UUID unidadeId = UUID.randomUUID();
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(10L, 5L, 5L),
            List.of(
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("RED", 5L, 5, false),
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("ORANGE", 10L, 10, false)
            )
        );

        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);

        assertNotNull(indicadores);
        assertEquals(unidadeId, indicadores.unidadeId());
        assertEquals(10, indicadores.ocupacaoAtual());
        assertEquals(5, indicadores.pacientesEmEspera());
        assertEquals(20, indicadores.capacidadeNominal());
        
        Map<RiskClassification, Integer> tmaPorRisco = indicadores.tmaPorRisco();
        assertEquals(5, tmaPorRisco.get(RiskClassification.RED));
        assertEquals(10, tmaPorRisco.get(RiskClassification.ORANGE));
    }

    @Test
    void devePreencherTmaPadraoParaRiscosNaoPresentes() {
        UUID unidadeId = UUID.randomUUID();
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(10L, 5L, 5L),
            List.of(
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("RED", 5L, 5, false)
            )
        );

        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);

        Map<RiskClassification, Integer> tmaPorRisco = indicadores.tmaPorRisco();
        assertEquals(5, tmaPorRisco.get(RiskClassification.RED));
        assertEquals(RiskClassification.ORANGE.getSlaMinutes(), tmaPorRisco.get(RiskClassification.ORANGE));
        assertEquals(RiskClassification.YELLOW.getSlaMinutes(), tmaPorRisco.get(RiskClassification.YELLOW));
        assertEquals(RiskClassification.GREEN.getSlaMinutes(), tmaPorRisco.get(RiskClassification.GREEN));
        assertEquals(RiskClassification.BLUE.getSlaMinutes(), tmaPorRisco.get(RiskClassification.BLUE));
    }

    @Test
    void deveLidarComAnalyticsNulo() {
        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(null);

        assertNotNull(indicadores);
        assertNotNull(indicadores.unidadeId());
        assertEquals(0, indicadores.ocupacaoAtual());
        assertEquals(0, indicadores.pacientesEmEspera());
        assertEquals(20, indicadores.capacidadeNominal());
    }

    @Test
    void deveLidarComQueueSnapshotNulo() {
        UUID unidadeId = UUID.randomUUID();
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            null,
            List.of()
        );

        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);

        assertNotNull(indicadores);
        assertEquals(0, indicadores.ocupacaoAtual());
        assertEquals(0, indicadores.pacientesEmEspera());
    }

    @Test
    void deveLidarComRiskPerformanceNulo() {
        UUID unidadeId = UUID.randomUUID();
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(10L, 5L, 5L),
            null
        );

        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);

        assertNotNull(indicadores);
        Map<RiskClassification, Integer> tmaPorRisco = indicadores.tmaPorRisco();
        assertTrue(tmaPorRisco.containsKey(RiskClassification.RED));
    }

    @Test
    void deveIgnorarRiscosDesconhecidos() {
        UUID unidadeId = UUID.randomUUID();
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            unidadeId.toString(),
            30L,
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(10L, 5L, 5L),
            List.of(
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("INVALID", 5L, 5, false),
                new UnitAnalyticsDTO.RiskAttendancePerformanceDTO("RED", 5L, 5, false)
            )
        );

        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);

        assertNotNull(indicadores);
        Map<RiskClassification, Integer> tmaPorRisco = indicadores.tmaPorRisco();
        assertEquals(5, tmaPorRisco.get(RiskClassification.RED));
    }

    @Test
    void deveLidarComHealthUnitIdInvalido() {
        UnitAnalyticsDTO analytics = new UnitAnalyticsDTO(
            "invalid-uuid",
            30L,
            new UnitAnalyticsDTO.LiveQueueSnapshotDTO(10L, 5L, 5L),
            List.of()
        );

        IndicadoresDTO indicadores = mapper.toIndicadoresDTO(analytics);

        assertNotNull(indicadores);
        assertNotNull(indicadores.unidadeId());
    }

    @Test
    void deveCriarIndicadoresPadrao() {
        UUID unidadeId = UUID.randomUUID();
        IndicadoresDTO indicadores = mapper.criarIndicadoresPadrao(unidadeId);

        assertNotNull(indicadores);
        assertEquals(unidadeId, indicadores.unidadeId());
        assertEquals(0, indicadores.ocupacaoAtual());
        assertEquals(0, indicadores.pacientesEmEspera());
        assertEquals(20, indicadores.capacidadeNominal());
        
        Map<RiskClassification, Integer> tmaPorRisco = indicadores.tmaPorRisco();
        assertEquals(5, tmaPorRisco.get(RiskClassification.RED));
        assertEquals(10, tmaPorRisco.get(RiskClassification.ORANGE));
        assertEquals(60, tmaPorRisco.get(RiskClassification.YELLOW));
        assertEquals(120, tmaPorRisco.get(RiskClassification.GREEN));
        assertEquals(240, tmaPorRisco.get(RiskClassification.BLUE));
    }

    @Test
    void deveCriarIndicadoresPadraoComUnidadeIdNulo() {
        IndicadoresDTO indicadores = mapper.criarIndicadoresPadrao(null);

        assertNotNull(indicadores);
        assertNotNull(indicadores.unidadeId());
    }
}
