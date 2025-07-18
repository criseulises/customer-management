package com.oriontek.customermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para crear un nuevo cliente.
 * Solo usuarios ADMIN y SUPERADMIN pueden crear clientes.
 */
@Schema(description = "Datos requeridos para crear un nuevo cliente")
public record CreateCustomerRequest(

        @Schema(description = "Nombre del cliente", example = "María")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String firstName,

        @Schema(description = "Apellido del cliente", example = "González")
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String lastName,

        @Schema(description = "Email del cliente", example = "maria.gonzalez@email.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @Schema(description = "Teléfono del cliente", example = "+1-809-555-1234")
        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String phone,

        @Schema(description = "Número de documento del cliente", example = "001-1234567-8")
        @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
        String documentNumber,

        @Schema(description = "Tipo de documento", example = "CEDULA", allowableValues = {"CEDULA", "PASAPORTE", "LICENCIA"})
        @Size(max = 50, message = "El tipo de documento no puede exceder 50 caracteres")
        String documentType,

        @Schema(description = "Notas adicionales sobre el cliente", example = "Cliente VIP - Preferencia por llamadas matutinas")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notes,

        @Schema(description = "Lista de direcciones del cliente (mínimo una)")
        @Valid
        List<CreateAddressRequest> addresses
) {

    /**
     * Constructor compacto para validaciones adicionales.
     */
    public CreateCustomerRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        if (phone != null) {
            phone = phone.trim();
        }
        if (documentNumber != null) {
            documentNumber = documentNumber.trim();
        }
        if (documentType != null) {
            documentType = documentType.trim().toUpperCase();
        }
        if (notes != null) {
            notes = notes.trim();
        }

        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("El cliente debe tener al menos una dirección");
        }

        long primaryCount = addresses.stream()
                .mapToLong(addr -> Boolean.TRUE.equals(addr.isPrimary()) ? 1 : 0)
                .sum();

        if (primaryCount == 0) {
            throw new IllegalArgumentException("Debe especificar una dirección como principal");
        }

        if (primaryCount > 1) {
            throw new IllegalArgumentException("Solo puede haber una dirección principal");
        }
    }
}