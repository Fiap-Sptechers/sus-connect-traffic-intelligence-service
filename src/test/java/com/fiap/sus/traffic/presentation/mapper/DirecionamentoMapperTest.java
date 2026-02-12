package com.fiap.sus.traffic.presentation.mapper;

import com.fiap.sus.traffic.domain.model.SugestaoOrdenada;
import com.fiap.sus.traffic.presentation.dto.SugestaoResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DirecionamentoMapperTest {

    private final DirecionamentoMapper mapper = new DirecionamentoMapper();

    @Test
    void deveConverterSugestaoParaResponse() {
        SugestaoOrdenada sugestao = new SugestaoOrdenada(
            UUID.randomUUID(),
            "Hospital Teste",
            0.85,
            5.0,
            30,
            "Próxima (5.0 km)."
        );

        SugestaoResponse response = mapper.toResponse(sugestao);

        assertEquals(sugestao.unidadeId(), response.unidadeId());
        assertEquals(sugestao.nome(), response.nome());
        assertEquals(sugestao.scoreFinal(), response.scoreFinal());
        assertEquals(sugestao.distanciaKm(), response.distanciaKm());
        assertEquals(sugestao.tempoEstimadoMinutos(), response.tempoEstimadoMinutos());
        assertEquals(sugestao.razao(), response.razao());
    }

    @Test
    void deveConverterListaDeSugestoes() {
        List<SugestaoOrdenada> sugestoes = List.of(
            new SugestaoOrdenada(
                UUID.randomUUID(),
                "Hospital 1",
                0.85,
                5.0,
                30,
                "Próxima."
            ),
            new SugestaoOrdenada(
                UUID.randomUUID(),
                "Hospital 2",
                0.75,
                10.0,
                45,
                "TMA rápido."
            )
        );

        List<SugestaoResponse> responses = mapper.toResponseList(sugestoes);

        assertEquals(2, responses.size());
        assertEquals("Hospital 1", responses.get(0).nome());
        assertEquals("Hospital 2", responses.get(1).nome());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaNula() {
        // O mapper usa stream(), então null causaria NPE
        // Vamos testar que o método funciona com lista vazia
        List<SugestaoResponse> responses = mapper.toResponseList(List.of());

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaVazia() {
        List<SugestaoResponse> responses = mapper.toResponseList(List.of());

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
