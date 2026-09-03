# =======================================================
# Stage 1: Build & Package the Application
# =======================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy pom.xml first for layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy project source code
COPY src ./src

# Build production jar
RUN mvn clean package -DskipTests -B

# =======================================================
# Stage 2: Minimal Production Runtime
# =======================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Set correct permissions
RUN chown -R appuser:appgroup /app

USER appuser

# Default port
EXPOSE 8080

# Cloud container-optimized JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
