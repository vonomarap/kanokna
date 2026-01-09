# Multi-stage build for pricing-service
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom files
COPY ../.mvn .mvn
COPY ../mvnw ../pom.xml ./
COPY pom.xml ./pricing-service/

# Download dependencies
RUN ./mvnw dependency:go-offline -pl pricing-service

# Copy source and build
COPY . ./pricing-service/
RUN ./mvnw clean package -pl pricing-service -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder
COPY --from=builder /app/pricing-service/target/pricing-service-*.jar app.jar

# Expose ports
EXPOSE 8082 9082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
