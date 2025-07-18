package com.oriontek.customermanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para documentación de la API.
 *
 * Esta configuración:
 * - Define información general de la API
 * - Configura autenticación JWT para Swagger UI
 * - Establece servidores de desarrollo y producción
 * - Añade esquemas de seguridad globales
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OrionTek Customer Management API",
                version = "1.0.0",
                description = """
            API RESTful para el sistema de gestión de clientes de OrionTek.
            
            ## Características principales:
            - Autenticación JWT con roles (SUPERADMIN, ADMIN)
            - Gestión de usuarios y administradores
            - Gestión de clientes con múltiples direcciones
            - Sistema de auditoría y logs
            - Seguridad robusta con Spring Security
            
            ## Autenticación:
            1. Haga login en `/api/auth/login` con credenciales válidas
            2. Use el token JWT devuelto en el header `Authorization: Bearer {token}`
            3. El token expira en 24 horas
            
            ## Usuarios por defecto:
            - **SUPERADMIN**: superadmin@oriontek.com / SuperAdmin123!
            - **ADMIN**: admin@oriontek.com / Admin123!
            
            ## Roles y permisos:
            - **SUPERADMIN**: Acceso completo al sistema, gestión de usuarios
            - **ADMIN**: Gestión de clientes y direcciones
            """,
                termsOfService = "https://oriontek.com/terms",
                contact = @Contact(
                        name = "OrionTek Development Team",
                        email = "dev@oriontek.com",
                        url = "https://oriontek.com"
                ),
                license = @License(
                        name = "Proprietary License",
                        url = "https://oriontek.com/license"
                )
        ),
        servers = {
                @Server(
                        description = "Desarrollo Local",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Staging",
                        url = "https://api-staging.oriontek.com"
                ),
                @Server(
                        description = "Producción",
                        url = "https://api.oriontek.com"
                )
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = """
        Autenticación JWT. 
        
        Pasos:
        1. Obtenga un token usando POST /api/auth/login
        2. Ingrese el token en el campo inferior (sin 'Bearer ')
        3. El token se enviará automáticamente en todas las requests
        """,
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

}