package com.oriontek.customermanagement.dto.response;

import com.oriontek.customermanagement.entity.Address;
import com.oriontek.customermanagement.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para respuestas que contienen información de direcciones.
 */
@Schema(description = "Información de una dirección")
public record AddressResponse(

        @Schema(description = "ID único de la dirección", example = "1")
        Long id,

        @Schema(description = "Calle y número", example = "Av. Winston Churchill #25")
        String street,

        @Schema(description = "Ciudad", example = "Santo Domingo")
        String city,

        @Schema(description = "Estado o provincia", example = "Distrito Nacional")
        String state,

        @Schema(description = "Código postal", example = "10101")
        String zipCode,

        @Schema(description = "País", example = "República Dominicana")
        String country,

        @Schema(description = "Dirección completa formateada")
        String fullAddress,

        @Schema(description = "Tipo de dirección", example = "HOME")
        AddressType type,

        @Schema(description = "Nombre del tipo de dirección", example = "Casa")
        String typeDisplayName,

        @Schema(description = "Si es la dirección principal", example = "true")
        Boolean isPrimary,

        @Schema(description = "Estado activo de la dirección", example = "true")
        Boolean active,

        @Schema(description = "Notas adicionales", example = "Portón azul")
        String notes,

        @Schema(description = "Fecha de creación")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt
) {

    /**
     * Método factory para crear AddressResponse desde una entidad Address.
     * @param address Entidad Address
     * @return AddressResponse con los datos de la dirección
     */
    public static AddressResponse fromEntity(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                address.getFullAddress(),
                address.getType(),
                address.getType().getDisplayName(),
                address.getIsPrimary(),
                address.getActive(),
                address.getNotes(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}