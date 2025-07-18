package com.oriontek.customermanagement.dto.request;

import com.oriontek.customermanagement.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear una nueva dirección.
 */
@Schema(description = "Datos requeridos para crear una nueva dirección")
public record CreateAddressRequest(

        @Schema(description = "Calle y número", example = "Av. Winston Churchill #25, Torre Empresarial, Piso 5")
        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 200, message = "La calle no puede exceder 200 caracteres")
        String street,

        @Schema(description = "Ciudad", example = "Santo Domingo")
        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        String city,

        @Schema(description = "Estado o provincia", example = "Distrito Nacional")
        @Size(max = 100, message = "El estado no puede exceder 100 caracteres")
        String state,

        @Schema(description = "Código postal", example = "10101")
        @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
        String zipCode,

        @Schema(description = "País", example = "República Dominicana")
        @NotBlank(message = "El país es obligatorio")
        @Size(max = 100, message = "El país no puede exceder 100 caracteres")
        String country,

        @Schema(description = "Tipo de dirección", example = "HOME")
        @NotNull(message = "El tipo de dirección es obligatorio")
        AddressType type,

        @Schema(description = "Si es la dirección principal", example = "true")
        Boolean isPrimary,

        @Schema(description = "Notas adicionales", example = "Portón azul, timbre #2")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notes
) {

    /**
     * Constructor compacto para validaciones adicionales.
     */
    public CreateAddressRequest {
        if (street != null) {
            street = street.trim();
        }
        if (city != null) {
            city = city.trim();
        }
        if (state != null) {
            state = state.trim();
        }
        if (zipCode != null) {
            zipCode = zipCode.trim();
        }
        if (country != null) {
            country = country.trim();
        }
        if (notes != null) {
            notes = notes.trim();
        }

        if (isPrimary == null) {
            isPrimary = false;
        }
    }
}