package com.fiap.sus.traffic.presentation.mapper;

import com.fiap.sus.traffic.domain.model.SugestaoOrdenada;
import com.fiap.sus.traffic.presentation.dto.SugestaoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DirecionamentoMapper {

    public SugestaoResponse toResponse(SugestaoOrdenada sugestao) {
        return new SugestaoResponse(
            sugestao.unidadeId(),
            sugestao.nome(),
            sugestao.scoreFinal(),
            sugestao.distanciaKm(),
            sugestao.tempoEstimadoMinutos(),
            sugestao.razao()
        );
    }

    public List<SugestaoResponse> toResponseList(List<SugestaoOrdenada> sugestoes) {
        return sugestoes.stream()
            .map(this::toResponse)
            .toList();
    }
}
