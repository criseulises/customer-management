# STAGE 1: compilación con Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Set encoding and locale
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8"

# 1) Sólo copiamos el pom para cachear dependencias
COPY pom.xml ./

# 2) Descargamos dependencias offline
RUN mvn dependency:go-offline -B

# 3) Copiamos el código (sin application.properties por .dockerignore)
COPY src/ src/

# 5) Compilamos
RUN mvn clean package -DskipTests -B -Dfile.encoding=UTF-8

# STAGE 2: runtime con JRE ligero
FROM openjdk:21-jdk-slim
WORKDIR /app

# Set encoding for runtime
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# 4) Traemos el JAR ya empaquetado
COPY --from=build /app/target/*.jar app.jar

# 5) Exponemos el puerto de la app
EXPOSE 8080

# 6) Punto de entrada
ENTRYPOINT ["java","-jar","app.jar"]