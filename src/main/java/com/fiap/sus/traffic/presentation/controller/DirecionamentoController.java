package com.fiap.sus.traffic.presentation.controller;

import com.fiap.sus.traffic.application.usecase.ConsultarDirecionamentoUseCase;
import com.fiap.sus.traffic.domain.model.SugestaoOrdenada;
import com.fiap.sus.traffic.presentation.dto.DirecionamentoRequest;
import com.fiap.sus.traffic.presentation.dto.DirecionamentoResponse;
import com.fiap.sus.traffic.presentation.mapper.DirecionamentoMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Direcionamento", description = "API para consulta de direcionamento inteligente de pacientes para unidades de saúde")
public class DirecionamentoController {

    private final ConsultarDirecionamentoUseCase consultarDirecionamentoUseCase;
    private final DirecionamentoMapper mapper;
    private final MeterRegistry meterRegistry;

    @Operation(
        summary = "Consultar direcionamento de paciente",
        description = """
            Consulta o direcionamento inteligente de um paciente para unidades de saúde próximas.
            
            O algoritmo considera:
            - **Distância geográfica**: Proximidade da unidade ao endereço do paciente
            - **Tempo médio de atendimento (TMA)**: Quanto menor, melhor
            - **Ocupação da unidade**: Quanto menor, melhor
            - **Especialidade médica**: Filtra unidades que oferecem a especialidade solicitada
            - **Classificação de risco**: Protocolo Manchester (RED, ORANGE, YELLOW, GREEN, BLUE)
            
            O resultado é uma lista ordenada de sugestões com score calculado, onde quanto maior o score, melhor a recomendação.
            """,
        operationId = "consultarDirecionamento"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Consulta realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DirecionamentoResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "sugestoes": [
                        {
                          "unidadeId": "550e8400-e29b-41d4-a716-446655440000",
                          "nome": "Hospital Central",
                          "endereco": "Av. Paulista, 1000, São Paulo, SP",
                          "distanciaKm": 2.5,
                          "tempoMedioAtendimentoMinutos": 15,
                          "ocupacaoPercentual": 45.0,
                          "especialidade": "Cardiologia",
                          "score": 0.85,
                          "justificativa": "Unidade próxima com baixa ocupação e TMA reduzido"
                        }
                      ],
                      "totalSugestoes": 1,
                      "tempoProcessamentoMs": 125
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - parâmetros incorretos ou faltando",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/consultar")
    public ResponseEntity<DirecionamentoResponse> consultar(
            @Parameter(
                description = "Parâmetros da consulta de direcionamento",
                required = true,
                schema = @Schema(implementation = DirecionamentoRequest.class)
            )
            @Valid DirecionamentoRequest request) {
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
