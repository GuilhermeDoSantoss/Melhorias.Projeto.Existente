# Stage 1: Build the application
FROM maven:3.8.5-openjdk-11 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Build the application JAR
# The Spring Boot Maven plugin should create an executable JAR
RUN mvn package -DskipTests


# Stage 2: Create the runtime image
FROM openjdk:11-jre-slim
WORKDIR /app

# Copy the JAR from the build stage
# Make sure the artifactId and version match your pom.xml (comunicacao_api-0.0.1-SNAPSHOT.jar)
COPY --from=build /app/target/comunicacao_api-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on (default 8080 for Spring Boot)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add a healthcheck (requires spring-boot-starter-actuator)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
#  CMD curl -f http://localhost:8080/actuator/health || exit 1 