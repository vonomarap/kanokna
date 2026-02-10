# Multi-stage Dockerfile for gateway

# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY gateway/pom.xml gateway/

# Download dependencies
RUN ./mvnw dependency:go-offline -B -pl gateway

# Copy source code
COPY gateway/src gateway/src

# Build application
RUN ./mvnw package -DskipTests -pl gateway
RUN mkdir -p gateway/target/dependency && \
     (cd gateway/target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

# Add non-root user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy artifacts from build stage
COPY --from=build /workspace/app/gateway/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/gateway/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/gateway/target/dependency/BOOT-INF/classes /app

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kanokna.gateway.GatewayApplication"]
