package com.oriontek.customermanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO específico para respuestas de autenticación.
 * Proporciona un formato consistente para todas las respuestas de auth.
 *
 * @param <T> Tipo de datos que contiene la respuesta
 */
@Schema(description = "Respuesta estándar para operaciones de autenticación")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse<T>(

        @Schema(description = "Indica si la autenticación fue exitosa", example = "true")
        boolean success,

        @Schema(description = "Mensaje descriptivo de la respuesta", example = "Login exitoso")
        String message,

        @Schema(description = "Datos de la respuesta de autenticación")
        T data,

        @Schema(description = "Código de error específico de auth (solo si success = false)", example = "AUTH_INVALID_CREDENTIALS")
        String errorCode,

        @Schema(description = "Timestamp de la respuesta")
        LocalDateTime timestamp
) {

    /**
     * Constructor compacto que asigna timestamp automáticamente.
     */
    public AuthResponse {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Crea una respuesta exitosa de login.
     * @param message Mensaje de éxito
     * @param loginData Datos de login (token + user info)
     * @return AuthResponse exitoso
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> loginSuccess(String message, T loginData) {
        return new AuthResponse<>(true, message, loginData, null, LocalDateTime.now());
    }

    /**
     * Crea una respuesta exitosa de logout.
     * @param message Mensaje de éxito
     * @return AuthResponse exitoso sin datos
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> logoutSuccess(String message) {
        return new AuthResponse<>(true, message, null, null, LocalDateTime.now());
    }

    /**
     * Crea una respuesta de credenciales inválidas.
     * @param message Mensaje de error
     * @return AuthResponse con error específico
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> invalidCredentials(String message) {
        return new AuthResponse<>(false, message, null, "AUTH_INVALID_CREDENTIALS", LocalDateTime.now());
    }

    /**
     * Crea una respuesta de usuario no encontrado.
     * @param message Mensaje de error
     * @return AuthResponse con error específico
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> userNotFound(String message) {
        return new AuthResponse<>(false, message, null, "AUTH_USER_NOT_FOUND", LocalDateTime.now());
    }

    /**
     * Crea una respuesta de usuario inactivo.
     * @param message Mensaje de error
     * @return AuthResponse con error específico
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> userInactive(String message) {
        return new AuthResponse<>(false, message, null, "AUTH_USER_INACTIVE", LocalDateTime.now());
    }

    /**
     * Crea una respuesta de token inválido.
     * @param message Mensaje de error
     * @return AuthResponse con error específico
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> invalidToken(String message) {
        return new AuthResponse<>(false, message, null, "AUTH_INVALID_TOKEN", LocalDateTime.now());
    }

    /**
     * Crea una respuesta de token expirado.
     * @param message Mensaje de error
     * @return AuthResponse con error específico
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> tokenExpired(String message) {
        return new AuthResponse<>(false, message, null, "AUTH_TOKEN_EXPIRED", LocalDateTime.now());
    }

    /**
     * Crea una respuesta de error genérico de autenticación.
     * @param message Mensaje de error
     * @param errorCode Código de error específico
     * @return AuthResponse con error
     * @param <T> Tipo de datos
     */
    public static <T> AuthResponse<T> authError(String message, String errorCode) {
        return new AuthResponse<>(false, message, null, errorCode, LocalDateTime.now());
    }
}