package com.fiap.sus.traffic.infrastructure.mapper;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NetworkServiceMapperTest {

    private final NetworkServiceMapper mapper = new NetworkServiceMapper();

    @Test
    void deveConverterHealthUnitResponseDTOParaUnidadeSaudeDTO() {
        UUID id = UUID.randomUUID();
        HealthUnitResponseDTO response = new HealthUnitResponseDTO(
            id,
            "Hospital Teste",
            "12345678000190",
            new HealthUnitResponseDTO.AddressResponseDTO(
                "Rua Teste",
                "123",
                "Sala 1",
                "Centro",
                "São Paulo",
                "SP",
                "01234567"
            ),
            List.of(
                new HealthUnitResponseDTO.ContactResponseDTO(
                    UUID.randomUUID(),
                    "11999999999",
                    "PHONE",
                    "Telefone principal"
                )
            ),
            "5.0 KM"
        );

        UnidadeSaudeDTO dto = mapper.toUnidadeSaudeDTO(response);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Hospital Teste", dto.name());
        assertEquals("12345678000190", dto.cnpj());
        assertNotNull(dto.address());
        assertEquals("Rua Teste", dto.address().street());
        assertEquals("123", dto.address().number());
        assertEquals(1, dto.contacts().size());
        assertEquals("5.0 KM", dto.distance());
    }

    @Test
    void deveRetornarNullQuandoResponseNulo() {
        UnidadeSaudeDTO dto = mapper.toUnidadeSaudeDTO(null);

        assertNull(dto);
    }

    @Test
    void deveLidarComAddressNulo() {
        UUID id = UUID.randomUUID();
        HealthUnitResponseDTO response = new HealthUnitResponseDTO(
            id,
            "Hospital Teste",
            "12345678000190",
            null,
            null,
            "5.0 KM"
        );

        UnidadeSaudeDTO dto = mapper.toUnidadeSaudeDTO(response);

        assertNotNull(dto);
        assertNull(dto.address());
        assertTrue(dto.contacts().isEmpty());
    }

    @Test
    void deveLidarComContactsNulo() {
        UUID id = UUID.randomUUID();
        HealthUnitResponseDTO response = new HealthUnitResponseDTO(
            id,
            "Hospital Teste",
            "12345678000190",
            null,
            null,
            "5.0 KM"
        );

        UnidadeSaudeDTO dto = mapper.toUnidadeSaudeDTO(response);

        assertNotNull(dto);
        assertTrue(dto.contacts().isEmpty());
    }

    @Test
    void deveConverterListaDeHealthUnitResponseDTO() {
        List<HealthUnitResponseDTO> responses = List.of(
            new HealthUnitResponseDTO(
                UUID.randomUUID(),
                "Hospital 1",
                "11111111000111",
                null,
                null,
                "5.0 KM"
            ),
            new HealthUnitResponseDTO(
                UUID.randomUUID(),
                "Hospital 2",
                "22222222000222",
                null,
                null,
                "10.0 KM"
            )
        );

        List<UnidadeSaudeDTO> dtos = mapper.toUnidadeSaudeDTOList(responses);

        assertEquals(2, dtos.size());
        assertEquals("Hospital 1", dtos.get(0).name());
        assertEquals("Hospital 2", dtos.get(1).name());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaNula() {
        List<UnidadeSaudeDTO> dtos = mapper.toUnidadeSaudeDTOList(null);

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaVazia() {
        List<UnidadeSaudeDTO> dtos = mapper.toUnidadeSaudeDTOList(List.of());

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void deveFiltrarElementosNulosNaLista() {
        // List.of() não aceita null, então usamos ArrayList
        List<HealthUnitResponseDTO> responses = new java.util.ArrayList<>();
        responses.add(new HealthUnitResponseDTO(
            UUID.randomUUID(),
            "Hospital 1",
            "11111111000111",
            null,
            null,
            "5.0 KM"
        ));
        responses.add(null);

        List<UnidadeSaudeDTO> dtos = mapper.toUnidadeSaudeDTOList(responses);

        assertEquals(1, dtos.size());
        assertEquals("Hospital 1", dtos.get(0).name());
    }
}
