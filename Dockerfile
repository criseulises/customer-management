# --- STAGE 1: Compilación con Maven instalado en la imagen ---
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# 1) Copia únicamente el POM para cachear dependencias
COPY pom.xml ./

# 2) Pre-descarga todas las dependencias (go-offline)
RUN mvn dependency:go-offline -B

# 3) Copia el código fuente y compila el JAR (sin tests)
COPY src/ src/
RUN mvn clean package -DskipTests -B

# --- STAGE 2: Runtime en JRE ligero ---
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 4) Trae el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# 5) Expón el puerto de la app (coincide con server.port)
EXPOSE 8080

# 6) Punto de entrada
ENTRYPOINT ["java","-jar","/app/app.jar"]