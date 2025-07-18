package com.oriontek.customermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de login.
 * Utiliza Java 21 Records para inmutabilidad y menos código boilerplate.
 */
@Schema(description = "Datos requeridos para autenticación de usuario")
public record LoginRequest(

        @Schema(description = "Email del usuario", example = "admin@oriontek.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @Schema(description = "Contraseña del usuario", example = "password123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password
) {

    /**
     * Constructor compacto para validaciones adicionales si fuera necesario.
     */
    public LoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}