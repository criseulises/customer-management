package com.oriontek.customermanagement.service;

import com.oriontek.customermanagement.dto.request.CreateUserRequest;
import com.oriontek.customermanagement.dto.request.UpdateUserRequest;
import com.oriontek.customermanagement.dto.response.UserResponse;
import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.enums.Role;
import com.oriontek.customermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de usuarios.
 * Solo SUPERADMIN puede crear, actualizar y gestionar usuarios ADMIN.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario ADMIN.
     * Solo SUPERADMIN puede ejecutar esta operación.
     *
     * @param request Datos del nuevo usuario
     * @return UserResponse con los datos del usuario creado
     * @throws RuntimeException Si el email ya existe o hay errores de validación
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Iniciando creación de usuario: {}", request.email());

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede crear usuarios");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Intento de crear usuario con email existente: {}", request.email());
            throw new RuntimeException("Ya existe un usuario con este email: " + request.email());
        }

        if (request.role() != Role.ADMIN) {
            throw new RuntimeException("Solo se pueden crear usuarios con rol ADMIN");
        }

        try {
            User newUser = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .role(request.role())
                    .active(true)
                    .build();

            User savedUser = userRepository.save(newUser);

            log.info("Usuario creado exitosamente: {} con ID: {}",
                    savedUser.getEmail(), savedUser.getId());

            return UserResponse.fromEntity(savedUser);

        } catch (Exception e) {
            log.error("Error al crear usuario {}: {}", request.email(), e.getMessage(), e);
            throw new RuntimeException("Error al crear usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los usuarios con paginación.
     * Solo SUPERADMIN puede ver todos los usuarios.
     *
     * @param pageable Información de paginación
     * @return Página de usuarios
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Obteniendo todos los usuarios con paginación");

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede ver todos los usuarios");
        }

        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Obtiene todos los usuarios ADMIN activos.
     *
     * @return Lista de usuarios ADMIN activos
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveAdmins() {
        log.debug("Obteniendo usuarios ADMIN activos");

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede ver usuarios ADMIN");
        }

        return userRepository.findByRoleAndActive(Role.ADMIN, true)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario
     * @return UserResponse con los datos del usuario
     * @throws RuntimeException Si el usuario no existe
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.debug("Obteniendo usuario por ID: {}", userId);

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new SecurityException("No tiene permisos para ver este usuario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        return UserResponse.fromEntity(user);
    }

    /**
     * Actualiza un usuario existente.
     * SUPERADMIN puede actualizar cualquier usuario.
     * ADMIN solo puede actualizar su propio perfil.
     *
     * @param userId ID del usuario a actualizar
     * @param request Datos a actualizar
     * @return UserResponse con los datos actualizados
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Actualizando usuario ID: {}", userId);

        if (!request.hasUpdates()) {
            throw new RuntimeException("No se proporcionaron datos para actualizar");
        }

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new SecurityException("Solo puede actualizar su propio perfil");
        }

        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (currentUser.getRole() == Role.ADMIN &&
                currentUser.getId().equals(userId) &&
                Boolean.FALSE.equals(request.active())) {
            throw new RuntimeException("No puede desactivar su propia cuenta");
        }

        try {
            if (request.email() != null) {
                if (!request.email().equals(userToUpdate.getEmail()) &&
                        userRepository.existsByEmail(request.email())) {
                    throw new RuntimeException("Ya existe un usuario con este email: " + request.email());
                }
                userToUpdate.setEmail(request.email());
            }

            if (request.firstName() != null) {
                userToUpdate.setFirstName(request.firstName());
            }

            if (request.lastName() != null) {
                userToUpdate.setLastName(request.lastName());
            }

            if (request.active() != null && currentUser.getRole() == Role.SUPERADMIN) {
                userToUpdate.setActive(request.active());
            }

            User updatedUser = userRepository.save(userToUpdate);

            log.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());
            return UserResponse.fromEntity(updatedUser);

        } catch (Exception e) {
            log.error("Error al actualizar usuario {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Desactiva un usuario (soft delete).
     * Solo SUPERADMIN puede desactivar usuarios.
     *
     * @param userId ID del usuario a desactivar
     */
    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Desactivando usuario ID: {}", userId);

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede desactivar usuarios");
        }

        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("No puede desactivar su propia cuenta");
        }

        User userToDeactivate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        userToDeactivate.setActive(false);
        userRepository.save(userToDeactivate);

        log.info("Usuario desactivado exitosamente: {}", userToDeactivate.getEmail());
    }

    /**
     * Activa un usuario previamente desactivado.
     * Solo SUPERADMIN puede activar usuarios.
     *
     * @param userId ID del usuario a activar
     */
    @Transactional
    public void activateUser(Long userId) {
        log.info("Activando usuario ID: {}", userId);

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede activar usuarios");
        }

        User userToActivate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        userToActivate.setActive(true);
        userRepository.save(userToActivate);

        log.info("Usuario activado exitosamente: {}", userToActivate.getEmail());
    }

    /**
     * Busca usuarios por término de búsqueda.
     * Solo SUPERADMIN puede buscar usuarios.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de usuarios que coinciden
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String searchTerm) {
        log.debug("Buscando usuarios con término: {}", searchTerm);

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede buscar usuarios");
        }

        return userRepository.findByNameOrEmailContaining(searchTerm)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    /**
     * Obtiene estadísticas de usuarios.
     * Solo SUPERADMIN puede ver estadísticas.
     *
     * @return Map con estadísticas
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        log.debug("Obteniendo estadísticas de usuarios");

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPERADMIN) {
            throw new SecurityException("Solo SUPERADMIN puede ver estadísticas");
        }

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByActiveTrue().size();
        long superAdmins = userRepository.countByRole(Role.SUPERADMIN);
        long admins = userRepository.countByRole(Role.ADMIN);

        return new UserStatistics(totalUsers, activeUsers, superAdmins, admins);
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
     * Record para estadísticas de usuarios.
     */
    public record UserStatistics(
            long totalUsers,
            long activeUsers,
            long superAdmins,
            long admins
    ) {}
}