# ============================================================
# EstateHub — Production Dockerfile
# ============================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the JAR
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q

# ── Runtime stage ──────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create directories for uploads and generated files
RUN mkdir -p /app/uploads/properties && \
    mkdir -p /app/logs

COPY --from=builder /app/target/estatehub-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
