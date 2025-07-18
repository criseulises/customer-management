# --- STAGE 1: Compilación ---
FROM maven:3.8.8-eclipse-temurin-17 AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos solo pom y wrapper para caché de dependencias
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Descarga dependencias (sin compilar aún)
RUN ./mvnw dependency:go-offline -B

# Copiamos el resto del código y compilamos el jar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# --- STAGE 2: Runtime ---
FROM eclipse-temurin:17-jdk-alpine

# Directorio donde vivirá la aplicación
WORKDIR /app

# Copiamos el jar generado desde el stage de build
COPY --from=build /app/target/*.jar app.jar

# Puerto que expondrá Spring Boot (coincide con server.port de application.properties)
EXPOSE 8080

# Comando de arranque: ejecuta el JAR
ENTRYPOINT ["java","-jar","/app/app.jar"]