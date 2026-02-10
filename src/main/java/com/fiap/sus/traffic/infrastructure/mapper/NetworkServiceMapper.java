package com.fiap.sus.traffic.infrastructure.mapper;

import com.fiap.sus.traffic.application.dto.UnidadeSaudeDTO;
import com.fiap.sus.traffic.infrastructure.client.dto.HealthUnitResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para converter HealthUnitResponseDTO (formato do Network Service) em UnidadeSaudeDTO (formato interno).
 */
@Component
public class NetworkServiceMapper {

    /**
     * Converte HealthUnitResponseDTO do Network Service para UnidadeSaudeDTO usado internamente.
     */
    public UnidadeSaudeDTO toUnidadeSaudeDTO(HealthUnitResponseDTO response) {
        if (response == null) {
            return null;
        }

        return new UnidadeSaudeDTO(
            response.id(),
            response.name(),
            response.cnpj(),
            response.address() != null ? new UnidadeSaudeDTO.AddressDTO(
                null, // AddressResponse do Network Service não tem id
                response.address().street(),
                response.address().number(),
                response.address().complement(),
                response.address().neighborhood(),
                response.address().city(),
                response.address().state(),
                response.address().zipCode()
            ) : null,
            response.contacts() != null 
                ? response.contacts().stream()
                    .map(contact -> new UnidadeSaudeDTO.ContactDTO(
                        contact.id(),
                        contact.value(),
                        contact.getTypeAsString() // Converte enum ou String para String
                    ))
                    .collect(Collectors.toList())
                : List.of(),
            response.distance()
        );
    }

    /**
     * Converte lista de HealthUnitResponseDTO para lista de UnidadeSaudeDTO.
     */
    public List<UnidadeSaudeDTO> toUnidadeSaudeDTOList(List<HealthUnitResponseDTO> responses) {
        if (responses == null || responses.isEmpty()) {
            return List.of();
        }

        return responses.stream()
            .map(this::toUnidadeSaudeDTO)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }
}
