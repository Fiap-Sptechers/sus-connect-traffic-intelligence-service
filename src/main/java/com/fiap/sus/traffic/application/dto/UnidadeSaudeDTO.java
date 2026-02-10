package com.fiap.sus.traffic.application.dto;

import java.util.List;
import java.util.UUID;

public record UnidadeSaudeDTO(
    UUID id,
    String name,
    String cnpj,
    AddressDTO address,
    List<ContactDTO> contacts,
    String distance
) {
    public record AddressDTO(
        UUID id,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode
    ) {}

    public record ContactDTO(
        UUID id,
        String value,
        String type
    ) {}
}
