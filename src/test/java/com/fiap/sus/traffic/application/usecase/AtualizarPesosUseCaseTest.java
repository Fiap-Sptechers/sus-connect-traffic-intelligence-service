package com.fiap.sus.traffic.application.usecase;

import com.fiap.sus.traffic.domain.model.CriterioPeso;
import com.fiap.sus.traffic.domain.repository.CriterioPesoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtualizarPesosUseCaseTest {

    @Mock
    private CriterioPesoRepository pesosRepository;

    @InjectMocks
    private AtualizarPesosUseCase useCase;

    @Test
    void deveAtualizarPesosComSucesso() {
        useCase.executar(0.3, 0.4, 0.2, 0.1);
        
        verify(pesosRepository, times(1)).salvar(any(CriterioPeso.class));
    }

    @Test
    void deveLancarExcecaoQuandoPesosInvalidos() {
        assertThrows(Exception.class, () -> {
            useCase.executar(0.5, 0.4, 0.2, 0.1); // Soma > 1.0
        });
    }
}
