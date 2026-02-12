package com.fiap.sus.traffic.application.usecase;

import com.fiap.sus.traffic.application.dto.IndicadoresDTO;
import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.application.port.CachePort;
import com.fiap.sus.traffic.application.port.LiveOpsServicePort;
import com.fiap.sus.traffic.application.port.NetworkServicePort;
import com.fiap.sus.traffic.core.exception.BusinessException;
import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.domain.model.*;
import com.fiap.sus.traffic.domain.repository.CriterioPesoRepository;
import com.fiap.sus.traffic.domain.service.AlgoritmoDirecionamentoService;
import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ConsultarDirecionamentoUseCaseTest {

    @Mock
    private NetworkServicePort networkServicePort;

    @Mock
    private LiveOpsServicePort liveOpsServicePort;

    @Mock
    private CriterioPesoRepository pesosRepository;

    @Mock
    private AlgoritmoDirecionamentoService algoritmoService;

    @Mock
    private TrafficIntelligenceProperties properties;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private ConsultarDirecionamentoUseCase useCase;

    private TrafficIntelligenceProperties.Algoritmo algoritmoConfig;
    private TrafficIntelligenceProperties.Cache cacheConfig;

    @BeforeEach
    void setUp() {
        algoritmoConfig = new TrafficIntelligenceProperties.Algoritmo();
        algoritmoConfig.setRaioDefaultKm(50.0);
        algoritmoConfig.setRaioMinimoKm(1.0);
        algoritmoConfig.setRaioMaximoKm(100.0);
        algoritmoConfig.setMaxSugestoes(5);

        cacheConfig = new TrafficIntelligenceProperties.Cache();
        cacheConfig.setTtlSugestoes(java.time.Duration.ofSeconds(300));

        lenient().when(properties.getAlgoritmo()).thenReturn(algoritmoConfig);
        lenient().when(properties.getCache()).thenReturn(cacheConfig);
    }

    @Test
    void deveLancarExcecaoQuandoBaseAddressNulo() {
        assertThrows(ValidationException.class, () -> {
            useCase.executar(null, RiskClassification.RED, null, null, null);
        });
    }

    @Test
    void deveLancarExcecaoQuandoBaseAddressVazio() {
        assertThrows(ValidationException.class, () -> {
            useCase.executar("   ", RiskClassification.RED, null, null, null);
        });
    }

    @Test
    void deveUsarRaioDefaultQuandoNaoFornecido() {
        UnidadeSaudeDTO unidadeDTO = criarUnidadeDTO();
        List<SugestaoOrdenada> sugestoes = criarSugestoes();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));
        when(liveOpsServicePort.buscarIndicadores(any())).thenReturn(criarIndicadoresDTO());
        when(pesosRepository.buscar()).thenReturn(Optional.of(CriterioPeso.padrao()));
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenReturn(sugestoes);

        List<SugestaoOrdenada> resultado = useCase.executar(
            "Rua Teste, 123", RiskClassification.RED, null, null, null
        );

        verify(networkServicePort).buscarUnidadesProximas(eq("Rua Teste, 123"), eq(50.0), eq("KM"));
        assertNotNull(resultado);
    }

    @Test
    void deveUsarDistanceUnitDefaultQuandoNaoFornecido() {
        UnidadeSaudeDTO unidadeDTO = criarUnidadeDTO();
        List<SugestaoOrdenada> sugestoes = criarSugestoes();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));
        when(liveOpsServicePort.buscarIndicadores(any())).thenReturn(criarIndicadoresDTO());
        when(pesosRepository.buscar()).thenReturn(Optional.of(CriterioPeso.padrao()));
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenReturn(sugestoes);

        useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, null);

        verify(networkServicePort).buscarUnidadesProximas(anyString(), anyDouble(), eq("KM"));
    }

    @Test
    void deveLancarExcecaoQuandoRaioMenorQueMinimo() {
        assertThrows(ValidationException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 0.5, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoRaioMaiorQueMaximo() {
        assertThrows(ValidationException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 150.0, "KM");
        });
    }

    @Test
    void deveRetornarSugestoesDoCacheQuandoDisponivel() {
        List<SugestaoOrdenada> sugestoesCache = criarSugestoes();
        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class)))
            .thenReturn(Optional.of(sugestoesCache));

        List<SugestaoOrdenada> resultado = useCase.executar(
            "Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM"
        );

        assertEquals(sugestoesCache, resultado);
        verify(networkServicePort, never()).buscarUnidadesProximas(anyString(), anyDouble(), anyString());
        verify(cachePort, never()).putSugestoes(anyString(), anyList(), anyLong());
    }

    @Test
    void deveLancarExcecaoQuandoNetworkServiceFalha() {
        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenThrow(new RuntimeException("Erro de conexão"));

        assertThrows(BusinessException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoNenhumaUnidadeEncontrada() {
        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesNull() {
        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");
        });
    }

    @Test
    void devePularUnidadeSemDistancia() {
        UnidadeSaudeDTO unidadeSemDistancia = new UnidadeSaudeDTO(
            UUID.randomUUID(),
            "Unidade Sem Distância",
            "12345678901234",
            null,
            List.of(),
            null
        );
        UnidadeSaudeDTO unidadeComDistancia = criarUnidadeDTO();
        List<SugestaoOrdenada> sugestoes = criarSugestoes();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeSemDistancia, unidadeComDistancia));
        when(liveOpsServicePort.buscarIndicadores(eq(unidadeComDistancia.id())))
            .thenReturn(criarIndicadoresDTO());
        when(pesosRepository.buscar()).thenReturn(Optional.of(CriterioPeso.padrao()));
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenReturn(sugestoes);

        List<SugestaoOrdenada> resultado = useCase.executar(
            "Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM"
        );

        assertNotNull(resultado);
        verify(liveOpsServicePort, times(1)).buscarIndicadores(any());
    }

    @Test
    void deveUsarIndicadoresPadraoQuandoLiveOpsFalha() {
        UnidadeSaudeDTO unidadeDTO = criarUnidadeDTO();
        List<SugestaoOrdenada> sugestoes = criarSugestoes();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));
        when(liveOpsServicePort.buscarIndicadores(any()))
            .thenThrow(new RuntimeException("Erro ao buscar indicadores"));
        when(pesosRepository.buscar()).thenReturn(Optional.of(CriterioPeso.padrao()));
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenReturn(sugestoes);

        List<SugestaoOrdenada> resultado = useCase.executar(
            "Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM"
        );

        assertNotNull(resultado);
    }

    @Test
    void deveUsarPesosPadraoQuandoNaoEncontrados() {
        UnidadeSaudeDTO unidadeDTO = criarUnidadeDTO();
        List<SugestaoOrdenada> sugestoes = criarSugestoes();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));
        when(liveOpsServicePort.buscarIndicadores(any())).thenReturn(criarIndicadoresDTO());
        when(pesosRepository.buscar()).thenReturn(Optional.empty());
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenReturn(sugestoes);

        useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");

        verify(algoritmoService).calcularSugestoes(
            anyList(),
            eq(CriterioPeso.padrao()),
            any(),
            any(),
            anyInt()
        );
    }

    @Test
    void deveArmazenarSugestoesNoCache() {
        UnidadeSaudeDTO unidadeDTO = criarUnidadeDTO();
        List<SugestaoOrdenada> sugestoes = criarSugestoes();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));
        when(liveOpsServicePort.buscarIndicadores(any())).thenReturn(criarIndicadoresDTO());
        when(pesosRepository.buscar()).thenReturn(Optional.of(CriterioPeso.padrao()));
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenReturn(sugestoes);

        useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");

        verify(cachePort).putSugestoes(anyString(), eq(sugestoes), eq(300L));
    }

    @Test
    void deveLancarExcecaoQuandoAlgoritmoFalha() {
        UnidadeSaudeDTO unidadeDTO = criarUnidadeDTO();

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));
        when(liveOpsServicePort.buscarIndicadores(any())).thenReturn(criarIndicadoresDTO());
        when(pesosRepository.buscar()).thenReturn(Optional.of(CriterioPeso.padrao()));
        when(algoritmoService.calcularSugestoes(anyList(), any(), any(), any(), anyInt()))
            .thenThrow(new RuntimeException("Erro no algoritmo"));

        assertThrows(BusinessException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");
        });
    }

    @Test
    void deveLancarExcecaoQuandoNenhumaUnidadeValidaAposProcessamento() {
        UnidadeSaudeDTO unidadeDTO = new UnidadeSaudeDTO(
            UUID.randomUUID(),
            "Unidade",
            "12345678901234",
            null,
            List.of(),
            "invalid distance format"
        );

        when(cachePort.getSugestoes(anyString(), eq(SugestaoOrdenada.class))).thenReturn(Optional.empty());
        when(networkServicePort.buscarUnidadesProximas(anyString(), anyDouble(), anyString()))
            .thenReturn(List.of(unidadeDTO));

        assertThrows(BusinessException.class, () -> {
            useCase.executar("Rua Teste, 123", RiskClassification.RED, null, 10.0, "KM");
        });
    }

    private UnidadeSaudeDTO criarUnidadeDTO() {
        return new UnidadeSaudeDTO(
            UUID.randomUUID(),
            "Hospital Teste",
            "12345678901234",
            null,
            List.of(),
            "5.0 km"
        );
    }

    private IndicadoresDTO criarIndicadoresDTO() {
        Map<RiskClassification, Integer> tmaPorRisco = new HashMap<>();
        tmaPorRisco.put(RiskClassification.RED, 5);
        tmaPorRisco.put(RiskClassification.ORANGE, 10);
        tmaPorRisco.put(RiskClassification.YELLOW, 60);
        tmaPorRisco.put(RiskClassification.GREEN, 120);
        tmaPorRisco.put(RiskClassification.BLUE, 240);

        return new IndicadoresDTO(
            UUID.randomUUID(),
            tmaPorRisco,
            5,
            2,
            10
        );
    }

    private List<SugestaoOrdenada> criarSugestoes() {
        return List.of(
            new SugestaoOrdenada(
                UUID.randomUUID(),
                "Hospital Teste",
                0.85,
                5.0,
                30,
                "Próxima (5.0 km)."
            )
        );
    }
}
