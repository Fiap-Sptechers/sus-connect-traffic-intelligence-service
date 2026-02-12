package com.fiap.sus.traffic.presentation.dto;

import com.fiap.sus.traffic.core.exception.ValidationException;
import com.fiap.sus.traffic.domain.model.RiskClassification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirecionamentoRequestTest {

    @Test
    void deveConverterRiskClassificationValido() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "RED",
            null,
            null,
            null
        );

        assertEquals(RiskClassification.RED, request.getRiskClassificationEnum());
    }

    @Test
    void deveConverterRiskClassificationCaseInsensitive() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "orange",
            null,
            null,
            null
        );

        assertEquals(RiskClassification.ORANGE, request.getRiskClassificationEnum());
    }

    @Test
    void deveLancarExcecaoQuandoRiskClassificationNulo() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            null,
            null,
            null,
            null
        );

        assertThrows(ValidationException.class, () -> {
            request.getRiskClassificationEnum();
        });
    }

    @Test
    void deveLancarExcecaoQuandoRiskClassificationVazio() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "   ",
            null,
            null,
            null
        );

        assertThrows(ValidationException.class, () -> {
            request.getRiskClassificationEnum();
        });
    }

    @Test
    void deveLancarExcecaoQuandoRiskClassificationInvalido() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "INVALID",
            null,
            null,
            null
        );

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            request.getRiskClassificationEnum();
        });

        assertTrue(exception.getMessage().contains("INVALID"));
        assertTrue(exception.getMessage().contains("RED, ORANGE, YELLOW, GREEN, BLUE"));
    }

    @Test
    void deveRetornarTodosValoresValidos() {
        DirecionamentoRequest request = new DirecionamentoRequest(
            "Rua Teste, 123",
            "YELLOW",
            "Cardiologia",
            10.0,
            "KM"
        );

        assertEquals("Rua Teste, 123", request.baseAddress());
        assertEquals("YELLOW", request.riskClassification());
        assertEquals("Cardiologia", request.especialidade());
        assertEquals(10.0, request.radius());
        assertEquals("KM", request.distanceUnit());
    }
}
