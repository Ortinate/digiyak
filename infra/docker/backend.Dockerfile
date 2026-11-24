# syntax=docker/dockerfile:1

FROM gradle:9.0.0-jdk25-alpine AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S digiyak && adduser -S digiyak -G digiyak
COPY --from=builder /app/build/libs/*.jar app.jar
USER digiyak
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
