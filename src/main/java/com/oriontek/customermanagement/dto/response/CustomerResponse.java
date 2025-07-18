package com.oriontek.customermanagement.dto.response;

import com.oriontek.customermanagement.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas que contienen información de clientes.
 */
@Schema(description = "Información completa de un cliente")
public record CustomerResponse(

        @Schema(description = "ID único del cliente", example = "1")
        Long id,

        @Schema(description = "Nombre del cliente", example = "María")
        String firstName,

        @Schema(description = "Apellido del cliente", example = "González")
        String lastName,

        @Schema(description = "Nombre completo del cliente", example = "María González")
        String fullName,

        @Schema(description = "Email del cliente", example = "maria.gonzalez@email.com")
        String email,

        @Schema(description = "Teléfono del cliente", example = "+1-809-555-1234")
        String phone,

        @Schema(description = "Número de documento", example = "001-1234567-8")
        String documentNumber,

        @Schema(description = "Tipo de documento", example = "CEDULA")
        String documentType,

        @Schema(description = "Estado activo del cliente", example = "true")
        Boolean active,

        @Schema(description = "Notas del cliente", example = "Cliente VIP")
        String notes,

        @Schema(description = "Lista de direcciones del cliente")
        List<AddressResponse> addresses,

        @Schema(description = "Usuario que creó el cliente")
        UserResponse createdBy,

        @Schema(description = "Fecha de creación")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt
) {

    /**
     * Método factory para crear CustomerResponse desde una entidad Customer.
     * @param customer Entidad Customer
     * @return CustomerResponse con los datos del cliente
     */
    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDocumentNumber(),
                customer.getDocumentType(),
                customer.getActive(),
                customer.getNotes(),
                customer.getAddresses().stream()
                        .map(AddressResponse::fromEntity)
                        .toList(),
                customer.getCreatedBy() != null ? UserResponse.fromEntity(customer.getCreatedBy()) : null,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    /**
     * Método factory para crear CustomerResponse básico (sin direcciones).
     * Útil para listados donde no necesitamos todas las direcciones.
     * @param customer Entidad Customer
     * @return CustomerResponse básico
     */
    public static CustomerResponse basicFromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDocumentNumber(),
                customer.getDocumentType(),
                customer.getActive(),
                customer.getNotes(),
                List.of(),
                customer.getCreatedBy() != null ? UserResponse.fromEntity(customer.getCreatedBy()) : null,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}