package com.oriontek.customermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar un usuario existente.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 */
@Schema(description = "Datos para actualizar un usuario existente (campos opcionales)")
public record UpdateUserRequest(

        @Schema(description = "Nuevo email del usuario", example = "newemail@oriontek.com")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @Schema(description = "Nuevo nombre del usuario", example = "Juan Carlos")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String firstName,

        @Schema(description = "Nuevo apellido del usuario", example = "Pérez García")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        String lastName,

        @Schema(description = "Estado activo del usuario", example = "true")
        Boolean active
) {

    /**
     * Constructor compacto para validaciones adicionales.
     */
    public UpdateUserRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
    }

    /**
     * Verifica si hay algún campo para actualizar.
     * @return true si al menos un campo no es null
     */
    public boolean hasUpdates() {
        return email != null || firstName != null || lastName != null || active != null;
    }
}