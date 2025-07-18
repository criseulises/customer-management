package com.oriontek.customermanagement.dto.response;

import com.oriontek.customermanagement.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de login exitoso.
 * Contiene el token JWT y la información básica del usuario.
 */
@Schema(description = "Respuesta exitosa de autenticación")
public record LoginResponse(

        @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Tipo de token", example = "Bearer")
        String type,

        @Schema(description = "Información del usuario autenticado")
        UserInfo user
) {

    /**
     * Método factory para crear la respuesta de login.
     * @param token Token JWT generado
     * @param userInfo Información del usuario
     * @return LoginResponse configurado
     */
    public static LoginResponse of(String token, UserInfo userInfo) {
        return new LoginResponse(token, "Bearer", userInfo);
    }

    /**
     * Record anidado para la información del usuario.
     * Solo incluye datos que son seguros exponer en el frontend.
     */
    @Schema(description = "Información básica del usuario")
    public record UserInfo(

            @Schema(description = "ID del usuario", example = "1")
            Long id,

            @Schema(description = "Email del usuario", example = "admin@oriontek.com")
            String email,

            @Schema(description = "Nombre del usuario", example = "John")
            String firstName,

            @Schema(description = "Apellido del usuario", example = "Doe")
            String lastName,

            @Schema(description = "Nombre completo del usuario", example = "John Doe")
            String fullName,

            @Schema(description = "Rol del usuario", example = "SUPERADMIN")
            Role role,

            @Schema(description = "Estado activo del usuario", example = "true")
            Boolean active,

            @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
            LocalDateTime createdAt
    ) {

        /**
         * Método factory para crear UserInfo desde una entidad User.
         * @param user Entidad User
         * @return UserInfo con los datos del usuario
         */
        public static UserInfo fromEntity(com.oriontek.customermanagement.entity.User user) {
            return new UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getFullName(),
                    user.getRole(),
                    user.getActive(),
                    user.getCreatedAt()
            );
        }
    }
}