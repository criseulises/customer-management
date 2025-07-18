package com.oriontek.customermanagement.service;

import com.oriontek.customermanagement.dto.request.CreateCustomerRequest;
import com.oriontek.customermanagement.dto.response.CustomerResponse;
import com.oriontek.customermanagement.entity.Address;
import com.oriontek.customermanagement.entity.Customer;
import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.enums.Role;
import com.oriontek.customermanagement.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de clientes.
 * ADMIN y SUPERADMIN pueden crear y gestionar clientes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Crea un nuevo cliente con sus direcciones.
     * Solo ADMIN y SUPERADMIN pueden crear clientes.
     *
     * @param request Datos del nuevo cliente
     * @return CustomerResponse con los datos del cliente creado
     * @throws RuntimeException Si el email ya existe o hay errores de validación
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Iniciando creación de cliente: {}", request.email());

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo ADMIN y SUPERADMIN pueden crear clientes");
        }

        if (customerRepository.existsByEmail(request.email())) {
            log.warn("Intento de crear cliente con email existente: {}", request.email());
            throw new RuntimeException("Ya existe un cliente con este email: " + request.email());
        }

        if (request.documentNumber() != null &&
                !request.documentNumber().trim().isEmpty() &&
                customerRepository.existsByDocumentNumber(request.documentNumber())) {
            log.warn("Intento de crear cliente con documento existente: {}", request.documentNumber());
            throw new RuntimeException("Ya existe un cliente con este número de documento: " + request.documentNumber());
        }

        try {
            Customer newCustomer = Customer.builder()
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .phone(request.phone())
                    .documentNumber(request.documentNumber())
                    .documentType(request.documentType())
                    .notes(request.notes())
                    .active(true)
                    .createdBy(currentUser)
                    .build();

            List<Address> addresses = request.addresses().stream()
                    .map(addressRequest -> Address.builder()
                            .street(addressRequest.street())
                            .city(addressRequest.city())
                            .state(addressRequest.state())
                            .zipCode(addressRequest.zipCode())
                            .country(addressRequest.country())
                            .type(addressRequest.type())
                            .isPrimary(addressRequest.isPrimary())
                            .notes(addressRequest.notes())
                            .active(true)
                            .customer(newCustomer)
                            .build())
                    .toList();

            addresses.forEach(newCustomer::addAddress);

            Customer savedCustomer = customerRepository.save(newCustomer);

            log.info("Cliente creado exitosamente: {} con ID: {} por usuario: {}",
                    savedCustomer.getEmail(), savedCustomer.getId(), currentUser.getEmail());

            return CustomerResponse.fromEntity(savedCustomer);

        } catch (Exception e) {
            log.error("Error al crear cliente {}: {}", request.email(), e.getMessage(), e);
            throw new RuntimeException("Error al crear cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los clientes con paginación.
     * SUPERADMIN ve todos los clientes, ADMIN solo los que creó.
     *
     * @param pageable Información de paginación
     * @return Página de clientes
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        log.debug("Obteniendo clientes con paginación");

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.SUPERADMIN) {
            return customerRepository.findByActiveTrue(pageable)
                    .map(CustomerResponse::basicFromEntity);
        } else if (currentUser.getRole() == Role.ADMIN) {
            return customerRepository.findByCreatedByAndActive(currentUser, true, pageable)
                    .map(CustomerResponse::basicFromEntity);
        } else {
            throw new SecurityException("No tiene permisos para ver clientes");
        }
    }

    /**
     * Obtiene un cliente por su ID con todas sus direcciones.
     * SUPERADMIN puede ver cualquier cliente, ADMIN solo los que creó.
     *
     * @param customerId ID del cliente
     * @return CustomerResponse con los datos completos del cliente
     * @throws RuntimeException Si el cliente no existe o no tiene permisos
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        log.debug("Obteniendo cliente por ID: {}", customerId);

        User currentUser = getCurrentUser();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + customerId));

        if (currentUser.getRole() == Role.ADMIN &&
                !customer.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permisos para ver este cliente");
        }

        return CustomerResponse.fromEntity(customer);
    }

    /**
     * Busca clientes por término de búsqueda.
     * SUPERADMIN busca en todos los clientes, ADMIN solo en los que creó.
     *
     * @param searchTerm Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de clientes que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String searchTerm, Pageable pageable) {
        log.debug("Buscando clientes con término: {}", searchTerm);

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.SUPERADMIN) {
            return customerRepository.searchActiveCustomers(searchTerm, true, pageable)
                    .map(CustomerResponse::basicFromEntity);
        } else if (currentUser.getRole() == Role.ADMIN) {
            Page<Customer> allResults = customerRepository.searchActiveCustomers(searchTerm, true, pageable);

            List<CustomerResponse> filteredResults = allResults.getContent().stream()
                    .filter(customer -> customer.getCreatedBy() != null &&
                            customer.getCreatedBy().getId().equals(currentUser.getId()))
                    .map(CustomerResponse::basicFromEntity)
                    .toList();

            return new org.springframework.data.domain.PageImpl<>(
                    filteredResults,
                    pageable,
                    filteredResults.size()
            );
        } else {
            throw new SecurityException("No tiene permisos para buscar clientes");
        }
    }

    /**
     * Desactiva un cliente (soft delete).
     * SUPERADMIN puede desactivar cualquier cliente, ADMIN solo los que creó.
     *
     * @param customerId ID del cliente a desactivar
     */
    @Transactional
    public void deactivateCustomer(Long customerId) {
        log.info("Desactivando cliente ID: {}", customerId);

        User currentUser = getCurrentUser();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + customerId));

        if (currentUser.getRole() == Role.ADMIN &&
                !customer.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permisos para desactivar este cliente");
        }

        customer.setActive(false);
        customerRepository.save(customer);

        log.info("Cliente desactivado exitosamente: {} por usuario: {}",
                customer.getEmail(), currentUser.getEmail());
    }

    /**
     * Activa un cliente previamente desactivado.
     * SUPERADMIN puede activar cualquier cliente, ADMIN solo los que creó.
     *
     * @param customerId ID del cliente a activar
     */
    @Transactional
    public void activateCustomer(Long customerId) {
        log.info("Activando cliente ID: {}", customerId);

        User currentUser = getCurrentUser();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + customerId));

        if (currentUser.getRole() == Role.ADMIN &&
                !customer.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permisos para activar este cliente");
        }

        customer.setActive(true);
        customerRepository.save(customer);

        log.info("Cliente activado exitosamente: {} por usuario: {}",
                customer.getEmail(), currentUser.getEmail());
    }

    /**
     * Obtiene estadísticas de clientes.
     * SUPERADMIN ve estadísticas globales, ADMIN solo de sus clientes.
     *
     * @return Estadísticas de clientes
     */
    @Transactional(readOnly = true)
    public CustomerStatistics getCustomerStatistics() {
        log.debug("Obteniendo estadísticas de clientes");

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.SUPERADMIN) {
            long totalCustomers = customerRepository.count();
            long activeCustomers = customerRepository.findByActiveTrue().size();

            return new CustomerStatistics(totalCustomers, activeCustomers, totalCustomers);
        } else if (currentUser.getRole() == Role.ADMIN) {
            long totalMyCustomers = customerRepository.countByCreatedBy(currentUser);
            long activeMyCustomers = customerRepository.countByCreatedByAndActive(currentUser, true);

            return new CustomerStatistics(totalMyCustomers, activeMyCustomers, totalMyCustomers);
        } else {
            throw new SecurityException("No tiene permisos para ver estadísticas");
        }
    }

    /**
     * Obtiene los clientes creados por un usuario específico.
     * Solo SUPERADMIN puede usar esta función.
     *
     * @param createdByUserId ID del usuario que creó los clientes
     * @param pageable Información de paginación
     * @return Página de clientes creados por el usuario
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomersByCreatedBy(Long createdByUserId, Pageable pageable) {
        log.debug("Obteniendo clientes creados por usuario ID: {}", createdByUserId);

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede ver clientes por usuario");
        }

        User createdByUser = new User();
        createdByUser.setId(createdByUserId);

        return customerRepository.findByCreatedByAndActive(createdByUser, true, pageable)
                .map(CustomerResponse::basicFromEntity);
    }

    /**
     * Obtiene el usuario actualmente autenticado.
     *
     * @return Usuario actual
     * @throws RuntimeException Si no hay usuario autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return (User) authentication.getPrincipal();
    }

    /**
     * Record para estadísticas de clientes.
     */
    public record CustomerStatistics(
            long totalCustomers,
            long activeCustomers,
            long managedCustomers
    ) {}
}