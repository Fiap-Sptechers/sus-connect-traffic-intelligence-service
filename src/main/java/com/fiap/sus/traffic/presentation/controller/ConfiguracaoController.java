package com.fiap.sus.traffic.presentation.controller;

import com.fiap.sus.traffic.application.usecase.AtualizarPesosUseCase;
import com.fiap.sus.traffic.presentation.dto.PesosConfigRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Slf4j
public class ConfiguracaoController {

    private final AtualizarPesosUseCase atualizarPesosUseCase;

    @PutMapping("/pesos")
    public ResponseEntity<Void> atualizarPesos(@Valid @RequestBody PesosConfigRequest request) {
        log.info("Atualizando configuração de pesos: {}", request);
        
        atualizarPesosUseCase.executar(
            request.pesoDistancia(),
            request.pesoTMA(),
            request.pesoOcupacao(),
            request.pesoEspecialidade()
        );

        return ResponseEntity.noContent().build();
    }
}
