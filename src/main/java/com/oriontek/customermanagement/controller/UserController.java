package com.oriontek.customermanagement.controller;

import com.oriontek.customermanagement.dto.request.CreateUserRequest;
import com.oriontek.customermanagement.dto.request.UpdateUserRequest;
import com.oriontek.customermanagement.dto.response.AuthResponse;
import com.oriontek.customermanagement.dto.response.UserResponse;
import com.oriontek.customermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios.
 * Solo SUPERADMIN puede acceder a estos endpoints.
 *
 * Endpoints disponibles:
 * - POST /api/admin/users - Crear usuario ADMIN
 * - GET /api/admin/users - Listar todos los usuarios
 * - GET /api/admin/users/{id} - Obtener usuario por ID
 * - PUT /api/admin/users/{id} - Actualizar usuario
 * - DELETE /api/admin/users/{id} - Desactivar usuario
 * - POST /api/admin/users/{id}/activate - Activar usuario
 * - GET /api/admin/users/search - Buscar usuarios
 * - GET /api/admin/users/statistics - Estadísticas de usuarios
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Gestión de usuarios (Solo SUPERADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Crea un nuevo usuario ADMIN.
     * Solo SUPERADMIN puede crear usuarios.
     */
    @Operation(
            summary = "Crear nuevo usuario ADMIN",
            description = "Crea un nuevo usuario con rol ADMIN. Solo SUPERADMIN puede ejecutar esta operación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Usuario creado exitosamente",
                        "data": {
                            "id": 2,
                            "email": "admin@oriontek.com",
                            "firstName": "Juan",
                            "lastName": "Pérez",
                            "fullName": "Juan Pérez",
                            "role": "ADMIN",
                            "active": true,
                            "createdAt": "2024-01-15T10:30:00",
                            "updatedAt": "2024-01-15T10:30:00"
                        },
                        "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos (solo SUPERADMIN)"),
            @ApiResponse(responseCode = "409", description = "Email ya existe")
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        try {
            log.info("Solicitud de creación de usuario: {}", request.email());

            UserResponse createdUser = userService.createUser(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.loginSuccess("Usuario creado exitosamente", createdUser));

        } catch (SecurityException e) {
            log.warn("Intento de creación de usuario sin permisos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para crear usuarios", "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            log.error("Error al crear usuario: {}", e.getMessage());
            if (e.getMessage().contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(AuthResponse.authError(e.getMessage(), "USER_EMAIL_EXISTS"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "USER_CREATION_ERROR"));
        }
    }

    /**
     * Obtiene todos los usuarios con paginación.
     */
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene una lista paginada de todos los usuarios del sistema"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        try {
            Page<UserResponse> users = userService.getAllUsers(pageable);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Usuarios obtenidos exitosamente", users)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver usuarios", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }

    /**
     * Obtiene usuarios ADMIN activos.
     */
    @Operation(
            summary = "Listar usuarios ADMIN activos",
            description = "Obtiene una lista de todos los usuarios ADMIN activos"
    )
    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<List<UserResponse>>> getActiveAdmins() {
        try {
            List<UserResponse> admins = userService.getActiveAdmins();

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Usuarios ADMIN obtenidos exitosamente", admins)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver usuarios", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }

    /**
     * Obtiene un usuario por su ID.
     */
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene los detalles de un usuario específico por su ID"
    )
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN') or (hasRole('ADMIN') and #userId == authentication.principal.id)")
    public ResponseEntity<AuthResponse<UserResponse>> getUserById(
            @Parameter(description = "ID del usuario") @PathVariable Long userId
    ) {
        try {
            UserResponse user = userService.getUserById(userId);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Usuario obtenido exitosamente", user)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver este usuario", "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.authError("Usuario no encontrado", "USER_NOT_FOUND"));
        }
    }

    /**
     * Actualiza un usuario existente.
     */
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos de un usuario existente. SUPERADMIN puede actualizar cualquier usuario, ADMIN solo su propio perfil."
    )
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN') or (hasRole('ADMIN') and #userId == authentication.principal.id)")
    public ResponseEntity<AuthResponse<UserResponse>> updateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        try {
            UserResponse updatedUser = userService.updateUser(userId, request);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Usuario actualizado exitosamente", updatedUser)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError(e.getMessage(), "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            log.error("Error al actualizar usuario {}: {}", userId, e.getMessage());
            if (e.getMessage().contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(AuthResponse.authError(e.getMessage(), "USER_EMAIL_EXISTS"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "USER_UPDATE_ERROR"));
        }
    }

    /**
     * Desactiva un usuario (soft delete).
     */
    @Operation(
            summary = "Desactivar usuario",
            description = "Desactiva un usuario del sistema. Solo SUPERADMIN puede desactivar usuarios."
    )
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Void>> deactivateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId
    ) {
        try {
            userService.deactivateUser(userId);

            return ResponseEntity.ok(
                    AuthResponse.logoutSuccess("Usuario desactivado exitosamente")
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError(e.getMessage(), "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "USER_DEACTIVATION_ERROR"));
        }
    }

    /**
     * Activa un usuario previamente desactivado.
     */
    @Operation(
            summary = "Activar usuario",
            description = "Activa un usuario previamente desactivado. Solo SUPERADMIN puede activar usuarios."
    )
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Void>> activateUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId
    ) {
        try {
            userService.activateUser(userId);

            return ResponseEntity.ok(
                    AuthResponse.logoutSuccess("Usuario activado exitosamente")
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError(e.getMessage(), "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "USER_ACTIVATION_ERROR"));
        }
    }

    /**
     * Busca usuarios por término de búsqueda.
     */
    @Operation(
            summary = "Buscar usuarios",
            description = "Busca usuarios por nombre, apellido o email"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<List<UserResponse>>> searchUsers(
            @Parameter(description = "Término de búsqueda") @RequestParam String term
    ) {
        try {
            List<UserResponse> users = userService.searchUsers(term);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Búsqueda completada", users)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para buscar usuarios", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }

    /**
     * Obtiene estadísticas de usuarios.
     */
    @Operation(
            summary = "Estadísticas de usuarios",
            description = "Obtiene estadísticas generales de usuarios del sistema"
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<UserService.UserStatistics>> getUserStatistics() {
        try {
            UserService.UserStatistics statistics = userService.getUserStatistics();

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Estadísticas obtenidas exitosamente", statistics)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver estadísticas", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }
}