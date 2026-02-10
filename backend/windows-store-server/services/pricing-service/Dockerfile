# Multi-stage Dockerfile for pricing-service

# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY services/pricing-service/pom.xml services/pricing-service/

# Download dependencies
RUN ./mvnw dependency:go-offline -B -pl services/pricing-service

# Copy source code
COPY services/pricing-service/src services/pricing-service/src

# Build application
RUN ./mvnw package -DskipTests -pl services/pricing-service
RUN mkdir -p services/pricing-service/target/dependency && \
  (cd services/pricing-service/target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

# Add non-root user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy artifacts from build stage
COPY --from=build /workspace/app/services/pricing-service/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/services/pricing-service/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/services/pricing-service/target/dependency/BOOT-INF/classes /app

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8082 9082

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kanokna.pricing.PricingServiceApplication"]
