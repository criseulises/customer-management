package com.oriontek.customermanagement.repository;

import com.oriontek.customermanagement.entity.Customer;
import com.oriontek.customermanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Customer.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Busca un cliente por email.
     * @param email Email del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Busca un cliente por email y que esté activo.
     * @param email Email del cliente
     * @param active Estado activo
     * @return Optional con el cliente si existe y está activo
     */
    Optional<Customer> findByEmailAndActive(String email, Boolean active);

    /**
     * Verifica si existe un cliente con el email dado.
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un cliente con el número de documento dado.
     * @param documentNumber Número de documento a verificar
     * @return true si existe, false si no
     */
    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Busca todos los clientes activos.
     * @return Lista de clientes activos
     */
    List<Customer> findByActiveTrue();

    /**
     * Busca todos los clientes activos con paginación.
     * @param pageable Información de paginación
     * @return Página de clientes activos
     */
    Page<Customer> findByActiveTrue(Pageable pageable);

    /**
     * Busca clientes por el usuario que los creó.
     * @param createdBy Usuario que creó los clientes
     * @return Lista de clientes creados por el usuario
     */
    List<Customer> findByCreatedBy(User createdBy);

    /**
     * Busca clientes activos por el usuario que los creó.
     * @param createdBy Usuario que creó los clientes
     * @param active Estado activo
     * @return Lista de clientes activos creados por el usuario
     */
    List<Customer> findByCreatedByAndActive(User createdBy, Boolean active);

    /**
     * Busca clientes activos por el usuario que los creó con paginación.
     * @param createdBy Usuario que creó los clientes
     * @param active Estado activo
     * @param pageable Información de paginación
     * @return Página de clientes activos creados por el usuario
     */
    Page<Customer> findByCreatedByAndActive(User createdBy, Boolean active, Pageable pageable);

    /**
     * Busca clientes creados en un rango de fechas.
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de clientes creados en el rango
     */
    List<Customer> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Query personalizada para buscar clientes por término de búsqueda.
     * Busca en nombre, apellido, email y número de documento.
     * @param searchTerm Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de clientes que coinciden con la búsqueda
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Query personalizada para buscar clientes activos por término de búsqueda.
     * @param searchTerm Término de búsqueda
     * @param active Estado activo
     * @param pageable Información de paginación
     * @return Página de clientes activos que coinciden con la búsqueda
     */
    @Query("SELECT c FROM Customer c WHERE c.active = :active AND (" +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> searchActiveCustomers(@Param("searchTerm") String searchTerm,
                                         @Param("active") Boolean active,
                                         Pageable pageable);

    /**
     * Cuenta clientes por usuario que los creó.
     * @param createdBy Usuario que creó los clientes
     * @return Número de clientes creados por el usuario
     */
    long countByCreatedBy(User createdBy);

    /**
     * Cuenta clientes activos por usuario que los creó.
     * @param createdBy Usuario que creó los clientes
     * @param active Estado activo
     * @return Número de clientes activos creados por el usuario
     */
    long countByCreatedByAndActive(User createdBy, Boolean active);
}