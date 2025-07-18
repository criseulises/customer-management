package com.oriontek.customermanagement.repository;

import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad User.
 * Extiende JpaRepository para operaciones CRUD automáticas.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por email (usado para login).
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por email y que esté activo.
     * @param email Email del usuario
     * @param active Estado activo
     * @return Optional con el usuario si existe y está activo
     */
    Optional<User> findByEmailAndActive(String email, Boolean active);

    /**
     * Verifica si existe un usuario con el email dado.
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Busca todos los usuarios activos.
     * @return Lista de usuarios activos
     */
    List<User> findByActiveTrue();

    /**
     * Busca usuarios por rol.
     * @param role Rol a buscar
     * @return Lista de usuarios con ese rol
     */
    List<User> findByRole(Role role);

    /**
     * Busca usuarios activos por rol.
     * @param role Rol a buscar
     * @param active Estado activo
     * @return Lista de usuarios activos con ese rol
     */
    List<User> findByRoleAndActive(Role role, Boolean active);

    /**
     * Cuenta usuarios por rol.
     * @param role Rol a contar
     * @return Número de usuarios con ese rol
     */
    long countByRole(Role role);

    /**
     * Query personalizada para buscar usuarios por nombre o apellido.
     * @param searchTerm Término de búsqueda
     * @return Lista de usuarios que coinciden
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameOrEmailContaining(String searchTerm);
}