# Etapa 1: build con Gradle (JDK 17)
FROM gradle:8.11-jdk17-alpine AS build

WORKDIR /home/gradle/project

# Copiamos todo el proyecto con permisos correctos
COPY --chown=gradle:gradle . .

# Construimos un fat JAR (incluye todas las dependencias)
# Si el plugin de Ktor genera la tarea `fatJar`, usamos esa
RUN gradle clean buildFatJar -x test --no-daemon

# Etapa 2: imagen ligera para correr el JAR
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/upet-backend-all.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]