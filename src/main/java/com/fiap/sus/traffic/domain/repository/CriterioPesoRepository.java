package com.fiap.sus.traffic.domain.repository;

import com.fiap.sus.traffic.domain.model.CriterioPeso;

import java.util.Optional;

public interface CriterioPesoRepository {
    Optional<CriterioPeso> buscar();
    void salvar(CriterioPeso pesos);
}
