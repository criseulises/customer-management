package com.oriontek.customermanagement.repository;

import com.oriontek.customermanagement.entity.Address;
import com.oriontek.customermanagement.entity.Customer;
import com.oriontek.customermanagement.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Address.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas de direcciones.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Busca todas las direcciones de un cliente.
     * @param customer Cliente
     * @return Lista de direcciones del cliente
     */
    List<Address> findByCustomer(Customer customer);

    /**
     * Busca todas las direcciones activas de un cliente.
     * @param customer Cliente
     * @param active Estado activo
     * @return Lista de direcciones activas del cliente
     */
    List<Address> findByCustomerAndActive(Customer customer, Boolean active);

    /**
     * Busca direcciones por tipo.
     * @param type Tipo de dirección
     * @return Lista de direcciones del tipo especificado
     */
    List<Address> findByType(AddressType type);

    /**
     * Busca direcciones de un cliente por tipo.
     * @param customer Cliente
     * @param type Tipo de dirección
     * @return Lista de direcciones del cliente del tipo especificado
     */
    List<Address> findByCustomerAndType(Customer customer, AddressType type);

    /**
     * Busca direcciones de un cliente por tipo y estado activo.
     * @param customer Cliente
     * @param type Tipo de dirección
     * @param active Estado activo
     * @return Lista de direcciones activas del cliente del tipo especificado
     */
    List<Address> findByCustomerAndTypeAndActive(Customer customer, AddressType type, Boolean active);

    /**
     * Busca la dirección principal de un cliente.
     * @param customer Cliente
     * @param isPrimary Si es dirección principal
     * @return Optional con la dirección principal si existe
     */
    Optional<Address> findByCustomerAndIsPrimary(Customer customer, Boolean isPrimary);

    /**
     * Busca la dirección principal activa de un cliente.
     * @param customer Cliente
     * @param isPrimary Si es dirección principal
     * @param active Estado activo
     * @return Optional con la dirección principal activa si existe
     */
    Optional<Address> findByCustomerAndIsPrimaryAndActive(Customer customer, Boolean isPrimary, Boolean active);

    /**
     * Cuenta las direcciones de un cliente.
     * @param customer Cliente
     * @return Número de direcciones del cliente
     */
    long countByCustomer(Customer customer);

    /**
     * Cuenta las direcciones activas de un cliente.
     * @param customer Cliente
     * @param active Estado activo
     * @return Número de direcciones activas del cliente
     */
    long countByCustomerAndActive(Customer customer, Boolean active);

    /**
     * Cuenta las direcciones principales de un cliente.
     * Debería ser máximo 1, pero útil para validaciones.
     * @param customer Cliente
     * @param isPrimary Si es dirección principal
     * @return Número de direcciones principales del cliente
     */
    long countByCustomerAndIsPrimary(Customer customer, Boolean isPrimary);

    /**
     * Busca direcciones por ciudad.
     * @param city Ciudad
     * @return Lista de direcciones en la ciudad especificada
     */
    List<Address> findByCity(String city);

    /**
     * Busca direcciones por país.
     * @param country País
     * @return Lista de direcciones en el país especificado
     */
    List<Address> findByCountry(String country);

    /**
     * Query personalizada para buscar direcciones por término de búsqueda.
     * Busca en calle, ciudad, estado y país.
     * @param searchTerm Término de búsqueda
     * @return Lista de direcciones que coinciden con la búsqueda
     */
    @Query("SELECT a FROM Address a WHERE " +
            "LOWER(a.street) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.state) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Address> searchAddresses(@Param("searchTerm") String searchTerm);

    /**
     * Query personalizada para buscar direcciones activas de un cliente por término.
     * @param customer Cliente
     * @param searchTerm Término de búsqueda
     * @param active Estado activo
     * @return Lista de direcciones que coinciden con la búsqueda
     */
    @Query("SELECT a FROM Address a WHERE a.customer = :customer AND a.active = :active AND (" +
            "LOWER(a.street) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.state) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.country) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Address> searchCustomerActiveAddresses(@Param("customer") Customer customer,
                                                @Param("searchTerm") String searchTerm,
                                                @Param("active") Boolean active);
}