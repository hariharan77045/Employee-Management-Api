# syntax=docker/dockerfile:1

# ---- Build stage: compile the Spring Boot jar with the Maven wrapper ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Download dependencies first so they are cached between builds.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Tests need a running PostgreSQL, so they run in CI (Jenkins) rather than here.
RUN ./mvnw -B -q package -DskipTests && cp target/*.jar app.jar

# ---- Runtime stage: small JRE image, non-root user ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --home /app app
COPY --from=build /app/app.jar app.jar
USER app

# Use up to 75% of the container's memory limit for the heap.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENV PORT=8080
EXPOSE 8080

# Health: GET /actuator/health (used by Kubernetes probes and Render's health check).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
