package com.fiap.sus.traffic.application.usecase;

import com.fiap.sus.traffic.domain.model.CriterioPeso;
import com.fiap.sus.traffic.domain.repository.CriterioPesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtualizarPesosUseCase {

    private final CriterioPesoRepository pesosRepository;

    public void executar(double pesoDistancia, double pesoTMA, 
                        double pesoOcupacao, double pesoEspecialidade) {
        
        log.info("Atualizando pesos do algoritmo: distancia={}, tma={}, ocupacao={}, especialidade={}",
            pesoDistancia, pesoTMA, pesoOcupacao, pesoEspecialidade);

        CriterioPeso pesos = CriterioPeso.builder()
            .pesoDistancia(pesoDistancia)
            .pesoTMA(pesoTMA)
            .pesoOcupacao(pesoOcupacao)
            .pesoEspecialidade(pesoEspecialidade)
            .build();

        pesosRepository.salvar(pesos);
        
        log.info("Pesos atualizados com sucesso");
    }
}
