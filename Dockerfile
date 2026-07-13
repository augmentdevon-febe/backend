FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]