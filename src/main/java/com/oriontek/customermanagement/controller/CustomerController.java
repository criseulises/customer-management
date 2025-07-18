package com.oriontek.customermanagement.controller;

import com.oriontek.customermanagement.dto.request.CreateCustomerRequest;
import com.oriontek.customermanagement.dto.response.AuthResponse;
import com.oriontek.customermanagement.dto.response.CustomerResponse;
import com.oriontek.customermanagement.service.CustomerService;
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

/**
 * Controlador REST para gestión de clientes.
 * ADMIN y SUPERADMIN pueden acceder a estos endpoints.
 *
 * Endpoints disponibles:
 * - POST /api/customers - Crear cliente
 * - GET /api/customers - Listar clientes
 * - GET /api/customers/{id} - Obtener cliente por ID
 * - DELETE /api/customers/{id} - Desactivar cliente
 * - POST /api/customers/{id}/activate - Activar cliente
 * - GET /api/customers/search - Buscar clientes
 * - GET /api/customers/statistics - Estadísticas de clientes
 * - GET /api/customers/by-user/{userId} - Clientes por usuario (Solo SUPERADMIN)
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Gestión de clientes (ADMIN y SUPERADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Crea un nuevo cliente con sus direcciones.
     * ADMIN y SUPERADMIN pueden crear clientes.
     */
    @Operation(
            summary = "Crear nuevo cliente",
            description = "Crea un nuevo cliente con al menos una dirección. ADMIN y SUPERADMIN pueden ejecutar esta operación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "success": true,
                        "message": "Cliente creado exitosamente",
                        "data": {
                            "id": 1,
                            "firstName": "María",
                            "lastName": "González",
                            "fullName": "María González",
                            "email": "maria.gonzalez@email.com",
                            "phone": "+1-809-555-1234",
                            "documentNumber": "001-1234567-8",
                            "documentType": "CEDULA",
                            "active": true,
                            "addresses": [
                                {
                                    "id": 1,
                                    "street": "Av. Winston Churchill #25",
                                    "city": "Santo Domingo",
                                    "country": "República Dominicana",
                                    "type": "HOME",
                                    "isPrimary": true,
                                    "active": true
                                }
                            ],
                            "createdAt": "2024-01-15T10:30:00"
                        },
                        "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "409", description = "Email o documento ya existe")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        try {
            log.info("Solicitud de creación de cliente: {}", request.email());

            CustomerResponse createdCustomer = customerService.createCustomer(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.loginSuccess("Cliente creado exitosamente", createdCustomer));

        } catch (SecurityException e) {
            log.warn("Intento de creación de cliente sin permisos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para crear clientes", "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            log.error("Error al crear cliente: {}", e.getMessage());
            if (e.getMessage().contains("email") || e.getMessage().contains("documento")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(AuthResponse.authError(e.getMessage(), "CUSTOMER_DUPLICATE_DATA"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "CUSTOMER_CREATION_ERROR"));
        }
    }

    /**
     * Obtiene todos los clientes con paginación.
     * SUPERADMIN ve todos, ADMIN solo los que creó.
     */
    @Operation(
            summary = "Listar clientes",
            description = "Obtiene una lista paginada de clientes. SUPERADMIN ve todos, ADMIN solo los que creó."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Page<CustomerResponse>>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        try {
            Page<CustomerResponse> customers = customerService.getAllCustomers(pageable);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Clientes obtenidos exitosamente", customers)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver clientes", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }

    /**
     * Obtiene un cliente por su ID con todas sus direcciones.
     */
    @Operation(
            summary = "Obtener cliente por ID",
            description = "Obtiene los detalles completos de un cliente específico por su ID, incluyendo todas sus direcciones"
    )
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<CustomerResponse>> getCustomerById(
            @Parameter(description = "ID del cliente") @PathVariable Long customerId
    ) {
        try {
            CustomerResponse customer = customerService.getCustomerById(customerId);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Cliente obtenido exitosamente", customer)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver este cliente", "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.authError("Cliente no encontrado", "CUSTOMER_NOT_FOUND"));
        }
    }

    /**
     * Busca clientes por término de búsqueda.
     */
    @Operation(
            summary = "Buscar clientes",
            description = "Busca clientes por nombre, apellido, email o número de documento"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Page<CustomerResponse>>> searchCustomers(
            @Parameter(description = "Término de búsqueda") @RequestParam String term,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        try {
            Page<CustomerResponse> customers = customerService.searchCustomers(term, pageable);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Búsqueda completada", customers)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para buscar clientes", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }

    /**
     * Desactiva un cliente (soft delete).
     */
    @Operation(
            summary = "Desactivar cliente",
            description = "Desactiva un cliente del sistema. SUPERADMIN puede desactivar cualquier cliente, ADMIN solo los que creó."
    )
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Void>> deactivateCustomer(
            @Parameter(description = "ID del cliente") @PathVariable Long customerId
    ) {
        try {
            customerService.deactivateCustomer(customerId);

            return ResponseEntity.ok(
                    AuthResponse.logoutSuccess("Cliente desactivado exitosamente")
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError(e.getMessage(), "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "CUSTOMER_DEACTIVATION_ERROR"));
        }
    }

    /**
     * Activa un cliente previamente desactivado.
     */
    @Operation(
            summary = "Activar cliente",
            description = "Activa un cliente previamente desactivado. SUPERADMIN puede activar cualquier cliente, ADMIN solo los que creó."
    )
    @PostMapping("/{customerId}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Void>> activateCustomer(
            @Parameter(description = "ID del cliente") @PathVariable Long customerId
    ) {
        try {
            customerService.activateCustomer(customerId);

            return ResponseEntity.ok(
                    AuthResponse.logoutSuccess("Cliente activado exitosamente")
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError(e.getMessage(), "AUTH_INSUFFICIENT_PERMISSIONS"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.authError(e.getMessage(), "CUSTOMER_ACTIVATION_ERROR"));
        }
    }

    /**
     * Obtiene estadísticas de clientes.
     */
    @Operation(
            summary = "Estadísticas de clientes",
            description = "Obtiene estadísticas de clientes. SUPERADMIN ve estadísticas globales, ADMIN solo de sus clientes."
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<CustomerService.CustomerStatistics>> getCustomerStatistics() {
        try {
            CustomerService.CustomerStatistics statistics = customerService.getCustomerStatistics();

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Estadísticas obtenidas exitosamente", statistics)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("No tiene permisos para ver estadísticas", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }

    /**
     * Obtiene clientes creados por un usuario específico.
     * Solo SUPERADMIN puede usar este endpoint.
     */
    @Operation(
            summary = "Clientes por usuario",
            description = "Obtiene todos los clientes creados por un usuario específico. Solo SUPERADMIN puede usar este endpoint."
    )
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AuthResponse<Page<CustomerResponse>>> getCustomersByUser(
            @Parameter(description = "ID del usuario que creó los clientes") @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        try {
            Page<CustomerResponse> customers = customerService.getCustomersByCreatedBy(userId, pageable);

            return ResponseEntity.ok(
                    AuthResponse.loginSuccess("Clientes obtenidos exitosamente", customers)
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.authError("Solo SUPERADMIN puede ver clientes por usuario", "AUTH_INSUFFICIENT_PERMISSIONS"));
        }
    }
}