package com.oriontek.customermanagement.dto.response;

import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para respuestas que contienen información de usuarios.
 * No incluye información sensible como contraseñas.
 */
@Schema(description = "Información de usuario en respuestas")
public record UserResponse(

        @Schema(description = "ID único del usuario", example = "1")
        Long id,

        @Schema(description = "Email del usuario", example = "admin@oriontek.com")
        String email,

        @Schema(description = "Nombre del usuario", example = "Juan")
        String firstName,

        @Schema(description = "Apellido del usuario", example = "Pérez")
        String lastName,

        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        String fullName,

        @Schema(description = "Rol del usuario", example = "ADMIN")
        Role role,

        @Schema(description = "Estado activo del usuario", example = "true")
        Boolean active,

        @Schema(description = "Fecha de creación del usuario")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt
) {

    /**
     * Método factory para crear UserResponse desde una entidad User.
     * @param user Entidad User
     * @return UserResponse con los datos del usuario
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}