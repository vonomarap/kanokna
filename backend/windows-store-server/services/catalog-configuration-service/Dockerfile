# Multi-stage Dockerfile for catalog-configuration-service

# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY catalog-configuration-service/pom.xml catalog-configuration-service/

# Download dependencies
RUN ./mvnw dependency:go-offline -B -pl catalog-configuration-service

# Copy source code
COPY catalog-configuration-service/src catalog-configuration-service/src

# Build application
RUN ./mvnw package -DskipTests -pl catalog-configuration-service
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

# Add non-root user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy artifacts from build stage
COPY --from=build /workspace/app/catalog-configuration-service/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/catalog-configuration-service/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/catalog-configuration-service/target/dependency/BOOT-INF/classes /app

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8081 9081

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kanokna.catalog.CatalogConfigurationServiceApplication"]
