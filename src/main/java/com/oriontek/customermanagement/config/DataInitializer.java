package com.oriontek.customermanagement.config;

import com.oriontek.customermanagement.entity.User;
import com.oriontek.customermanagement.enums.Role;
import com.oriontek.customermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 *
 * Responsabilidades:
 * - Crear usuario SUPERADMIN por defecto si no existe
 * - Crear usuarios de prueba en desarrollo
 * - Verificar integridad de datos esenciales
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.superadmin.email}")
    private String superAdminEmail;

    @Value("${app.default.superadmin.password}")
    private String superAdminPassword;

    @Value("${app.default.admin.email}")
    private String adminEmail;

    @Value("${app.default.admin.password}")
    private String adminPassword;

    @Value("${app.environment}")
    private String environment;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Iniciando configuración de datos para entorno: {}", environment.toUpperCase());

        createDefaultSuperAdmin();

        if ("development".equals(environment)) {
            createDefaultAdminUser();
        }

        reportUserStatistics();

        if ("production".equals(environment)) {
            logProductionSecurityWarnings();
        }

        log.info("✅ Configuración de datos completada para entorno: {}", environment.toUpperCase());
    }

    /**
     * Crea el usuario SUPERADMIN por defecto si no existe.
     */
    private void createDefaultSuperAdmin() {
        if (userRepository.findByEmail(superAdminEmail).isEmpty()) {
            User superAdmin = User.builder()
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .firstName("Super")
                    .lastName("Admin")
                    .role(Role.SUPERADMIN)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);

            log.info("✅ Usuario SUPERADMIN creado exitosamente:");
            log.info("   📧 Email: {}", superAdminEmail);

            if ("development".equals(environment)) {
                log.info("   🔑 Password: {}", superAdminPassword);
            } else {
                log.info("   🔑 Password: *** (configurado desde variables de entorno)");
            }

            log.info("   👤 Rol: SUPERADMIN");

        } else {
            log.info("ℹ️  Usuario SUPERADMIN ya existe, omitiendo creación.");
        }
    }

    /**
     * Crea un usuario ADMIN por defecto para pruebas en desarrollo.
     */
    private void createDefaultAdminUser() {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("Admin")
                    .lastName("OrionTek")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);

            log.info("✅ Usuario ADMIN de desarrollo creado exitosamente:");
            log.info("   📧 Email: {}", adminEmail);
            log.info("   🔑 Password: {}", adminPassword);
            log.info("   👤 Rol: ADMIN");

        } else {
            log.info("ℹ️  Usuario ADMIN ya existe, omitiendo creación.");
        }
    }

    /**
     * Verifica y reporta estadísticas de usuarios.
     */
    private void reportUserStatistics() {
        long totalUsers = userRepository.count();
        long superAdmins = userRepository.countByRole(Role.SUPERADMIN);
        long admins = userRepository.countByRole(Role.ADMIN);

        log.info("📊 Estadísticas de usuarios:");
        log.info("   👥 Total de usuarios: {}", totalUsers);
        log.info("   🔱 Super Administradores: {}", superAdmins);
        log.info("   👨‍💼 Administradores: {}", admins);
        log.info("   🌍 Entorno: {}", environment.toUpperCase());
    }

    /**
     * Muestra advertencias de seguridad para producción.
     */
    private void logProductionSecurityWarnings() {
        log.warn("⚠️  ===== ADVERTENCIAS DE SEGURIDAD PARA PRODUCCIÓN =====");
        log.warn("⚠️  1. Verifique que JWT_SECRET sea único y seguro (mínimo 256 bits)");
        log.warn("⚠️  2. Cambie las contraseñas por defecto de SUPERADMIN y ADMIN");
        log.warn("⚠️  3. Configure DATABASE_PASSWORD con una contraseña segura");
        log.warn("⚠️  4. Revise CORS_ALLOWED_ORIGINS para dominios de producción");
        log.warn("⚠️  5. Considere usar HTTPS y certificados SSL");
        log.warn("⚠️  6. Configure DDL_AUTO=validate para evitar cambios en esquema");
        log.warn("⚠️  =================================================");
    }
}