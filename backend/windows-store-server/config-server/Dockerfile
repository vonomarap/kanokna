# Multi-stage Dockerfile for config-server

# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY config-server/pom.xml config-server/

# Download dependencies
RUN ./mvnw dependency:go-offline -B -pl config-server

# Copy source code
COPY config-server/src config-server/src

# Build application
RUN ./mvnw package -DskipTests -pl config-server
RUN mkdir -p config-server/target/dependency && \
     (cd config-server/target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

# Add non-root user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy artifacts from build stage
COPY --from=build /workspace/app/config-server/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/config-server/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/config-server/target/dependency/BOOT-INF/classes /app

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8888 8889

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kanokna.config_server.ConfigServerApplication"]
