package com.fiap.sus.traffic.presentation.controller;

import com.fiap.sus.traffic.application.usecase.ConsultarDirecionamentoUseCase;
import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import com.fiap.sus.traffic.domain.model.SugestaoOrdenada;
import com.fiap.sus.traffic.presentation.dto.DirecionamentoRequest;
import com.fiap.sus.traffic.presentation.mapper.DirecionamentoMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirecionamentoControllerTest {

    @Mock
    private ConsultarDirecionamentoUseCase consultarDirecionamentoUseCase;

    @Mock
    private DirecionamentoMapper mapper;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private DirecionamentoController controller;

    @BeforeEach
    void setUp() {
        // Usar SimpleMeterRegistry real em vez de mock para evitar problemas com Timer.start()
        meterRegistry = new SimpleMeterRegistry();
        // Injetar manualmente pois @InjectMocks não funciona bem com campos não-mockados
        controller = new DirecionamentoController(
            consultarDirecionamentoUseCase,
            mapper,
            meterRegistry
        );
    }

    @Test
    void deveConsultarDirecionamentoComSucesso() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "RED",
            "Cardiologia",
            10.0,
            "KM"
        );

        List<SugestaoOrdenada> sugestoes = List.of(
            new SugestaoOrdenada(
                UUID.randomUUID(),
                "Hospital Teste",
                0.85,
                5.0,
                30,
                "Próxima (5.0 km)."
            )
        );

        when(consultarDirecionamentoUseCase.executar(
            eq("Rua Teste, 123"),
            eq(RiskClassification.RED),
            eq("Cardiologia"),
            eq(10.0),
            eq("KM")
        )).thenReturn(sugestoes);

        when(mapper.toResponseList(sugestoes)).thenReturn(
            sugestoes.stream().map(s -> new com.fiap.sus.traffic.presentation.dto.SugestaoResponse(
                s.unidadeId(),
                s.nome(),
                s.scoreFinal(),
                s.distanciaKm(),
                s.tempoEstimadoMinutos(),
                s.razao()
            )).toList()
        );

        var response = controller.consultar(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().totalUnidadesAnalisadas());
        verify(consultarDirecionamentoUseCase).executar(
            eq("Rua Teste, 123"),
            eq(RiskClassification.RED),
            eq("Cardiologia"),
            eq(10.0),
            eq("KM")
        );
    }

    @Test
    void deveLancarExcecaoQuandoRadiusNegativo() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "RED",
            null,
            -1.0,
            "KM"
        );

        // O controller valida radius <= 0 ANTES de chamar o use case, então deve lançar ValidationException
        assertThrows(ValidationException.class, () -> {
            controller.consultar(request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoRadiusZero() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "RED",
            null,
            0.0,
            "KM"
        );

        // O controller valida radius <= 0 ANTES de chamar o use case, então deve lançar ValidationException
        assertThrows(ValidationException.class, () -> {
            controller.consultar(request);
        });
    }

    @Test
    void deveRegistrarMetricas() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "RED",
            null,
            10.0,
            "KM"
        );

        List<SugestaoOrdenada> sugestoes = List.of(
            new SugestaoOrdenada(
                UUID.randomUUID(),
                "Hospital Teste",
                0.85,
                5.0,
                30,
                "Próxima (5.0 km)."
            )
        );

        when(consultarDirecionamentoUseCase.executar(anyString(), any(), any(), any(), anyString()))
            .thenReturn(sugestoes);
        when(mapper.toResponseList(anyList())).thenReturn(List.of());

        // Executar - o SimpleMeterRegistry permite que Timer.start() funcione
        var response = controller.consultar(request);

        assertNotNull(response);
        verify(consultarDirecionamentoUseCase).executar(anyString(), any(), any(), any(), anyString());
        
        // Verificar que métricas foram registradas
        assertTrue(meterRegistry.getMeters().size() > 0);
    }
}
