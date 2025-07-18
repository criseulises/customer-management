package com.oriontek.customermanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para manejo de tokens JWT.
 * Responsable de generar, validar y extraer información de los tokens.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Extrae el username (email) del token JWT.
     * @param token Token JWT
     * @return Username extraído del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token.
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token.
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim
     * @return Valor del claim
     * @param <T> Tipo del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token.
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error al extraer claims del token: {}", e.getMessage());
            throw new RuntimeException("Token JWT inválido", e);
        }
    }

    /**
     * Verifica si el token ha expirado.
     * @param token Token JWT
     * @return true si el token ha expirado
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error al verificar expiración del token: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Genera un token JWT para el usuario.
     * @param userDetails Detalles del usuario
     * @return Token JWT generado
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("authorities", userDetails.getAuthorities());
        extraClaims.put("enabled", userDetails.isEnabled());

        return generateToken(extraClaims, userDetails);
    }

    /**
     * Genera un token JWT con claims adicionales.
     * @param extraClaims Claims adicionales
     * @param userDetails Detalles del usuario
     * @return Token JWT generado
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Construye el token JWT.
     * @param extraClaims Claims adicionales
     * @param userDetails Detalles del usuario
     * @param expiration Tiempo de expiración en millisegundos
     * @return Token JWT construido
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        try {
            Date now = new Date(System.currentTimeMillis());
            Date expirationDate = new Date(System.currentTimeMillis() + expiration);

            String token = Jwts.builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(now)
                    .expiration(expirationDate)
                    .signWith(getSignInKey())
                    .compact();

            log.info("Token JWT generado exitosamente para usuario: {}", userDetails.getUsername());
            return token;

        } catch (Exception e) {
            log.error("Error al generar token JWT para usuario {}: {}",
                    userDetails.getUsername(), e.getMessage());
            throw new RuntimeException("Error al generar token JWT", e);
        }
    }

    /**
     * Valida si el token es válido para el usuario.
     * @param token Token JWT
     * @param userDetails Detalles del usuario
     * @return true si el token es válido
     */
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error al validar token para usuario {}: {}",
                    userDetails.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la clave de firma para el JWT.
     * @return Clave de firma
     */
    private javax.crypto.SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el token del header Authorization.
     * @param authHeader Header de autorización
     * @return Token JWT sin el prefijo "Bearer "
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Verifica si el token tiene el formato correcto.
     * @param token Token a verificar
     * @return true si el formato es válido
     */
    public Boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    /**
     * Obtiene información del token para logs (sin datos sensibles).
     * @param token Token JWT
     * @return Información básica del token
     */
    public String getTokenInfo(String token) {
        try {
            if (!isValidTokenFormat(token)) {
                return "Token con formato inválido";
            }

            String username = extractUsername(token);
            Date expiration = extractExpiration(token);
            boolean expired = isTokenExpired(token);

            return String.format("Usuario: %s, Expira: %s, Expirado: %s",
                    username, expiration, expired);
        } catch (Exception e) {
            return "Token inválido: " + e.getMessage();
        }
    }
}