package com.fiap.sus.traffic.presentation.controller;

import com.fiap.sus.traffic.application.usecase.ConsultarDirecionamentoUseCase;
import com.fiap.sus.traffic.domain.model.SugestaoOrdenada;
import com.fiap.sus.traffic.presentation.dto.DirecionamentoRequest;
import com.fiap.sus.traffic.presentation.dto.DirecionamentoResponse;
import com.fiap.sus.traffic.presentation.mapper.DirecionamentoMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/direcionamento")
@RequiredArgsConstructor
@Slf4j
public class DirecionamentoController {

    private final ConsultarDirecionamentoUseCase consultarDirecionamentoUseCase;
    private final DirecionamentoMapper mapper;
    private final MeterRegistry meterRegistry;

    @RequestMapping(value = "/consultar", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<DirecionamentoResponse> consultar(@Valid DirecionamentoRequest request) {
        long inicio = System.currentTimeMillis();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        Counter consultasCounter = Counter.builder("traffic.intelligence.consultas.total")
            .description("Total de consultas de direcionamento")
            .register(meterRegistry);

        log.info("Recebida requisição de direcionamento: {}", request);
        
        // Validar radius se fornecido (validação adicional além do @Valid)
        if (request.radius() != null && request.radius() <= 0) {
            throw new com.fiap.sus.traffic.core.exception.ValidationException("radius", "Raio deve ser maior que zero");
        }
        
        List<SugestaoOrdenada> sugestoes = consultarDirecionamentoUseCase.executar(
            request.baseAddress(),
            request.getRiskClassificationEnum(),
            request.especialidade(),
            request.radius(),
            request.distanceUnit()
        );

        long tempoProcessamento = System.currentTimeMillis() - inicio;
        
        DirecionamentoResponse response = new DirecionamentoResponse(
            mapper.toResponseList(sugestoes),
            sugestoes.size(),
            tempoProcessamento
        );

        sample.stop(Timer.builder("traffic.intelligence.consultas.duracao")
            .description("Duração das consultas de direcionamento")
            .register(meterRegistry));

        consultasCounter.increment();
        
        meterRegistry.counter("traffic.intelligence.unidades.analisadas", 
            "total", String.valueOf(sugestoes.size())).increment();

        log.info("Direcionamento concluído em {}ms. {} sugestões geradas", 
            tempoProcessamento, sugestoes.size());

        return ResponseEntity.ok(response);
    }
}
