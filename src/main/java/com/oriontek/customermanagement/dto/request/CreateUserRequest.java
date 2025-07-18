package com.oriontek.customermanagement.dto.request;

import com.oriontek.customermanagement.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear un nuevo usuario (ADMIN).
 * Solo SUPERADMIN puede crear usuarios ADMIN.
 */
@Schema(description = "Datos requeridos para crear un nuevo usuario")
public record CreateUserRequest(

        @Schema(description = "Email del usuario", example = "admin@oriontek.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @Schema(description = "Contraseña del usuario", example = "SecurePassword123!")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @Schema(description = "Nombre del usuario", example = "Juan")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String firstName,

        @Schema(description = "Apellido del usuario", example = "Pérez")
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        String lastName,

        @Schema(description = "Rol del usuario", example = "ADMIN", allowableValues = {"ADMIN"})
        @NotNull(message = "El rol es obligatorio")
        Role role
) {

    /**
     * Constructor compacto para validaciones adicionales.
     */
    public CreateUserRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }

        if (role != null && role != Role.ADMIN) {
            throw new IllegalArgumentException("Solo se pueden crear usuarios con rol ADMIN");
        }
    }
}