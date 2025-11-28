# Etapa 1: build con Gradle
FROM gradle:8.9-jdk17-alpine AS build

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN gradle build --no-daemon

# Etapa 2: imagen ligera para correr el JAR
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

