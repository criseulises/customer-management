package com.oriontek.customermanagement.service;

import com.oriontek.customermanagement.dto.request.LoginRequest;
import com.oriontek.customermanagement.dto.response.LoginResponse;
import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación que maneja el proceso de login.
 *
 * Responsabilidades:
 * - Validar credenciales de usuario
 * - Generar tokens JWT
 * - Manejar errores de autenticación
 * - Auditar intentos de login
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param loginRequest Credenciales del usuario
     * @return LoginResponse con token y datos del usuario
     * @throws AuthenticationException Si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();

        log.info("Iniciando proceso de autenticación para usuario: {}", email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            log.debug("Credenciales validadas exitosamente para usuario: {}", email);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;
            if (!user.getActive()) {
                log.warn("Intento de login de usuario inactivo: {}", email);
                throw new DisabledException("Usuario inactivo");
            }

            String jwtToken = jwtService.generateToken(userDetails);
            log.info("Token JWT generado exitosamente para usuario: {}", email);

            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.fromEntity(user);
            LoginResponse response = LoginResponse.of(jwtToken, userInfo);

            log.info("Login exitoso para usuario: {} con rol: {}", email, user.getRole());
            return response;

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para usuario: {}", email);
            throw new BadCredentialsException("Email o contraseña incorrectos");

        } catch (DisabledException e) {
            log.warn("Usuario deshabilitado intento hacer login: {}", email);
            throw new DisabledException("Cuenta de usuario deshabilitada");

        } catch (AuthenticationException e) {
            log.error("Error de autenticación para usuario {}: {}", email, e.getMessage());
            throw new BadCredentialsException("Error en la autenticación: " + e.getMessage());

        } catch (Exception e) {
            log.error("Error inesperado durante autenticación para usuario {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error interno durante la autenticación");
        }
    }

    /**
     * Valida un token JWT y devuelve información del usuario.
     * Útil para endpoints que necesitan verificar la validez del token.
     *
     * @param token Token JWT a validar
     * @return UserInfo si el token es válido
     * @throws RuntimeException Si el token es inválido
     */
    @Transactional(readOnly = true)
    public LoginResponse.UserInfo validateToken(String token) {
        try {
            String email = jwtService.extractUsername(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new RuntimeException("Token JWT inválido");
            }

            User user = (User) userDetails;
            log.debug("Token válido para usuario: {}", email);

            return LoginResponse.UserInfo.fromEntity(user);

        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            throw new RuntimeException("Token inválido: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario existe y está activo.
     * Útil para validaciones antes del login.
     *
     * @param email Email del usuario
     * @return true si el usuario existe y está activo
     */
    public boolean userExistsAndActive(String email) {
        try {
            return userDetailsService.existsByEmail(email);
        } catch (Exception e) {
            log.error("Error al verificar existencia del usuario {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información básica del usuario sin autenticar.
     * Útil para recuperación de contraseña o verificaciones.
     *
     * @param email Email del usuario
     * @return UserInfo básica o null si no existe
     */
    @Transactional(readOnly = true)
    public LoginResponse.UserInfo getUserInfo(String email) {
        try {
            User user = userDetailsService.getUserByEmail(email);
            return LoginResponse.UserInfo.fromEntity(user);
        } catch (Exception e) {
            log.debug("Usuario no encontrado: {}", email);
            return null;
        }
    }

    /**
     * Extrae el token del header Authorization.
     *
     * @param authorizationHeader Header Authorization completo
     * @return Token sin el prefijo "Bearer " o null si es inválido
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        return jwtService.extractTokenFromHeader(authorizationHeader);
    }

    /**
     * Método para auditar intentos de login (se puede expandir).
     *
     * @param email Email del usuario
     * @param successful Si el login fue exitoso
     * @param reason Razón del fallo (si aplica)
     */
    private void auditLoginAttempt(String email, boolean successful, String reason) {
        if (successful) {
            log.info("LOGIN_SUCCESS - Usuario: {}", email);
        } else {
            log.warn("LOGIN_FAILED - Usuario: {}, Razón: {}", email, reason);
        }

    }
}