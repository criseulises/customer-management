# OrionTek Customer Management System

<p align="center">
  <a href="https://customer-management-production-f510.up.railway.app/swagger-ui/index.html#/">
    <img src="https://img.shields.io/badge/Swagger-OpenAPI-blue" alt="Swagger UI"/>
  </a>
  <img src="https://img.shields.io/badge/Build-Passing-brightgreen" alt="Build Status"/>
  <img src="https://img.shields.io/badge/Java-21-blue" alt="Java Version"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.3-brightgreen" alt="Spring Boot Version"/>
  <img src="https://img.shields.io/badge/Maven-3.8+-blue" alt="Maven Version"/>
  <img src="https://img.shields.io/badge/Docker-Enabled-lightgrey" alt="Docker"/>
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License"/>
</p>

---

## 📖 Descripción

Aplicación de gestión de clientes creada como **entrevista técnica para OrionTek**.  
Permite **crear**, **listar**, **buscar**, **activar/desactivar** y **consultar estadísticas** de clientes y usuarios con múltiples direcciones.
<br/> <b>OJO: Agregue creación de perfiles para poder manejar agregar los clientes, además de autenticación con JWT.<b/>
---

## 🚀 Características Principales

- **Autenticación JWT** (24h) con roles `SUPERADMIN` y `ADMIN`.
- **Usuarios**: CRUD completo, búsqueda y estadísticas (solo `SUPERADMIN`).
- **Clientes**: CRUD completo, búsqueda, paginación y estadísticas (`ADMIN` ve solo sus clientes).
- **Soft delete** para clientes y usuarios.
- **Documentación automática** con Swagger UI.
- **Configuración por perfiles**: `development` y `production`.
- **Inicialización de datos** (superadmin y admin en dev).
- **Contenerización** con Docker.

---

## 🛠️ Tech Stack

| Categoría              | Tecnología                                           |
|------------------------|------------------------------------------------------|
| Lenguaje               | Java 21                                              |
| Framework Web          | Spring Boot 3.5.3                                    |
| Seguridad              | Spring Security + JWT                                |
| Persistencia           | Spring Data JPA, Hibernate, HikariCP                 |
| Validación             | Jakarta Validation                                   |
| Construcción           | Maven                                                |
| Contenedores & Cloud   | Docker, Railway                                      |
| Documentación          | springdoc-openapi (Swagger UI)                       |
| Logging                | SLF4J + Logback                                      |
| Data Initialization    | CommandLineRunner                                    |

---

## 🌐 Despliegue y Acceso

1. **Base de datos MySQL** en Railway.
2. **Microservicio** contenerizado con Docker y desplegado en Railway.
3. **URL de Swagger UI**:  
   [https://customer-management-production-f510.up.railway.app/swagger-ui/index.html#/](https://customer-management-production-f510.up.railway.app/swagger-ui/index.html#/)

---

## 📂 Estructura del Proyecto

```
customer-management/
├── src
│   ├── main
│   │   ├── java/com/oriontek/customermanagement
│   │   │   ├── config         # DataInitializer, seguridad y perfiles
│   │   │   ├── controller     # Endpoints REST
│   │   │   ├── dto            # Requests & Responses
│   │   │   ├── entity         # JPA Entities
│   │   │   ├── enums          # Roles, tipos de documento, etc.
│   │   │   ├── repository     # Spring Data JPA Repos
│   │   │   ├── security       # JWT filter y configuración
│   │   │   └── service        # Lógica de negocio
│   └── resources
│       └── application.yml    # Configuración externa
├── pom.xml                   # Dependencias y plugins
├── Dockerfile                # Contenerización
└── README.md                 # Documentación
```

---

## 🔧 Quick Start

1. Clonar:
   ```bash
   git clone <URL_DEL_REPO>
   cd customer-management
   ```
2. Configurar variables de entorno (no incluir en VCS):
   - `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
   - `JWT_SECRET`, `JWT_EXPIRATION`, `CORS_ALLOWED_ORIGINS`
   - `APP_ENVIRONMENT`, `DDL_AUTO`, `LOG_LEVEL`, etc.
3. Compilar y ejecutar con Maven:
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```
4. Acceder a Swagger UI: `http://localhost:8080/swagger-ui/index.html#/`

---

## 📜 Licencia

MIT © OrionTek
