package com.oriontek.customermanagement.controller;

import com.oriontek.customermanagement.dto.request.LoginRequest;
import com.oriontek.customermanagement.dto.response.AuthResponse;
import com.oriontek.customermanagement.dto.response.LoginResponse;
import com.oriontek.customermanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación.
 *
 * Endpoints disponibles:
 * - POST /api/auth/login - Autenticar usuario
 * - POST /api/auth/validate - Validar token
 * - GET /api/auth/me - Obtener información del usuario actual
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operaciones de autenticación y autorización")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para autenticar un usuario.
     *
     * @param loginRequest Credenciales del usuario (email y contraseña)
     * @param request Request HTTP para logging adicional
     * @return Token JWT y datos del usuario si la autenticación es exitosa
     */
    @Operation(
            summary = "Autenticar usuario",
            description = "Autentica un usuario con email y contraseña, devuelve un token JWT válido por 24 horas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Login exitoso",
                        "data": {
                            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "type": "Bearer",
                            "user": {
                                "id": 1,
                                "email": "admin@oriontek.com",
                                "firstName": "Admin",
                                "lastName": "User",
                                "fullName": "Admin User",
                                "role": "SUPERADMIN",
                                "active": true,
                                "createdAt": "2024-01-15T10:30:00"
                            }
                        },
                        "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Email o contraseña incorrectos",
                        "errorCode": "AUTH_INVALID_CREDENTIALS",
                        "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario inactivo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": false,
                        "message": "Cuenta de usuario deshabilitada",
                        "errorCode": "AUTH_USER_INACTIVE",
                        "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        String clientIp = getClientIpAddress(request);
        log.info("Intento de login para usuario: {} desde IP: {}", loginRequest.email(), clientIp);

        try {
            LoginResponse loginResponse = authService.authenticate(loginRequest);

            log.info("Login exitoso para usuario: {} desde IP: {}", loginRequest.email(), clientIp);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Login exitoso", loginResponse)
            );

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para usuario: {} desde IP: {}", loginRequest.email(), clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.invalidCredentials("Email o contraseña incorrectos"));

        } catch (DisabledException e) {
            log.warn("Usuario inactivo intento login: {} desde IP: {}", loginRequest.email(), clientIp);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.userInactive("Cuenta de usuario deshabilitada"));

        } catch (Exception e) {
            log.error("Error interno durante login para usuario: {} desde IP: {}",
                    loginRequest.email(), clientIp, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.authError("Error interno del servidor", "AUTH_INTERNAL_ERROR"));
        }
    }

    /**
     * Endpoint para validar un token JWT.
     *
     * @param authorizationHeader Header Authorization con el token
     * @return Información del usuario si el token es válido
     */
    @Operation(
            summary = "Validar token JWT",
            description = "Valida un token JWT y devuelve la información del usuario asociado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token válido",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado"
            )
    })
    @PostMapping("/validate")
    public ResponseEntity<AuthResponse<LoginResponse.UserInfo>> validateToken(
            @Parameter(description = "Token JWT en formato 'Bearer {token}'")
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authService.extractTokenFromHeader(authorizationHeader);

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.invalidToken("Token no encontrado en el header"));
            }

            LoginResponse.UserInfo userInfo = authService.validateToken(token);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Token válido", userInfo)
            );

        } catch (Exception e) {
            log.warn("Token inválido recibido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.invalidToken("Token inválido o expirado"));
        }
    }

    /**
     * Endpoint para obtener información del usuario actualmente autenticado.
     *
     * @param authorizationHeader Header Authorization con el token
     * @return Información del usuario actual
     */
    @Operation(
            summary = "Obtener usuario actual",
            description = "Devuelve la información del usuario actualmente autenticado"
    )
    @GetMapping("/me")
    public ResponseEntity<AuthResponse<LoginResponse.UserInfo>> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authService.extractTokenFromHeader(authorizationHeader);

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.invalidToken("Token no encontrado"));
            }

            LoginResponse.UserInfo userInfo = authService.validateToken(token);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Información del usuario obtenida", userInfo)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.invalidToken("No se pudo obtener información del usuario"));
        }
    }

    /**
     * Endpoint para hacer logout (invalidar token del lado del cliente).
     * Como JWT es stateless, el logout real se hace eliminando el token del frontend.
     */
    @Operation(
            summary = "Cerrar sesión",
            description = "Indica al cliente que debe eliminar el token JWT (logout del lado del cliente)"
    )
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse<Void>> logout() {
        log.info("Logout solicitado - se debe eliminar token del cliente");

        return ResponseEntity.ok(
                AuthResponse.logoutSuccess("Logout exitoso - elimine el token del cliente")
        );
    }

    /**
     * Obtiene la dirección IP real del cliente considerando proxies y load balancers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}