package com.fiap.sus.traffic.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * DTO para receber HealthUnitResponse do Network Service.
 * Estrutura deve corresponder exatamente ao que o Network Service retorna.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HealthUnitResponseDTO(
    @JsonProperty("id") UUID id,
    @JsonProperty("name") String name,
    @JsonProperty("cnpj") String cnpj,
    @JsonProperty("address") AddressResponseDTO address,
    @JsonProperty("contacts") List<ContactResponseDTO> contacts,
    @JsonProperty("distance") String distance
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressResponseDTO(
        @JsonProperty("street") String street,
        @JsonProperty("number") String number,
        @JsonProperty("complement") String complement,
        @JsonProperty("neighborhood") String neighborhood,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("zipCode") String zipCode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactResponseDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("value") String value,
        @JsonProperty("type") Object type, // Pode ser String ou enum ContactType
        @JsonProperty("description") String description
    ) {
        /**
         * Converte o tipo para String, lidando com enum ou String.
         */
        public String getTypeAsString() {
            if (type == null) {
                return null;
            }
            if (type instanceof String) {
                return (String) type;
            }
            // Se for enum, usa toString()
            return type.toString();
        }
    }
}
