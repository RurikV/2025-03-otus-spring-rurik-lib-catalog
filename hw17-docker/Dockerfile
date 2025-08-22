# syntax=docker/dockerfile:1

# -------- Build stage --------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only this module's sources for better layer caching
COPY pom.xml ./
COPY src ./src

# Build this module (tests skipped for faster image builds)
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user (Debian/Ubuntu base)
RUN groupadd -r app && useradd -r -g app app

# Copy fat jar from build stage
COPY --from=build /workspace/target/hw17-docker-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Run as non-root
USER app

# Use JAVA_TOOL_OPTIONS so JVM picks up options without shell wrapper
ENV JAVA_TOOL_OPTIONS=""

# Log to stdout/stderr by default (Spring Boot default)
ENTRYPOINT ["java","-jar","/app/app.jar"]
