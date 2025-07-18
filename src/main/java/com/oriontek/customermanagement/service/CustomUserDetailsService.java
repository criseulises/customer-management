package com.oriontek.customermanagement.service;

import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación personalizada de UserDetailsService para Spring Security.
 * Este servicio es responsable de cargar los detalles del usuario desde la base de datos
 * durante el proceso de autenticación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su username (email en nuestro caso).
     * Este método es llamado automáticamente por Spring Security durante la autenticación.
     *
     * @param username El email del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Intentando cargar usuario por email: {}", username);

        try {
            User user = userRepository.findByEmailAndActive(username, true)
                    .orElseThrow(() -> {
                        log.warn("Usuario no encontrado o inactivo: {}", username);
                        return new UsernameNotFoundException(
                                "Usuario no encontrado con email: " + username
                        );
                    });

            log.info("Usuario cargado exitosamente: {} con rol: {}",
                    user.getEmail(), user.getRole());

            return user;

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al cargar usuario {}: {}", username, e.getMessage());
            throw new UsernameNotFoundException(
                    "Error al cargar usuario: " + username, e
            );
        }
    }

    /**
     * Método helper para verificar si un usuario existe por email.
     * Útil para validaciones adicionales.
     *
     * @param email Email del usuario
     * @return true si el usuario existe y está activo
     */
    public boolean existsByEmail(String email) {
        try {
            return userRepository.findByEmailAndActive(email, true).isPresent();
        } catch (Exception e) {
            log.error("Error al verificar existencia del usuario {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Método helper para obtener un usuario por email.
     * Útil para servicios que necesitan acceso directo a la entidad User.
     *
     * @param email Email del usuario
     * @return User entity si existe
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    public User getUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailAndActive(email, true)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email
                ));
    }

    /**
     * Método helper para refrescar los datos del usuario.
     * Útil cuando se actualizan los datos del usuario y necesitamos
     * los datos más recientes de la base de datos.
     *
     * @param userId ID del usuario
     * @return UserDetails con datos actualizados
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    public UserDetails refreshUserDetails(Long userId) throws UsernameNotFoundException {
        log.debug("Refrescando datos del usuario con ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                    "Usuario inactivo con ID: " + userId
            );
        }

        log.debug("Datos del usuario refrescados: {}", user.getEmail());
        return user;
    }
}