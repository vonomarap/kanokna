# Multi-stage Dockerfile for search-service

# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY shared/shared-kernel/pom.xml shared/shared-kernel/
COPY api-contracts/pom.xml api-contracts/
COPY services/search-service/pom.xml services/search-service/

# Download dependencies
RUN ./mvnw dependency:go-offline -B -pl services/search-service -am

# Copy source code for dependencies and the service itself
COPY shared/shared-kernel/src shared/shared-kernel/src
COPY api-contracts/src api-contracts/src
COPY services/search-service/src services/search-service/src

# Build application
RUN ./mvnw package -DskipTests -pl services/search-service -am
RUN mkdir -p services/search-service/target/dependency && \
    (cd services/search-service/target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

# Add non-root user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy artifacts from build stage
COPY --from=build /workspace/app/services/search-service/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/services/search-service/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/services/search-service/target/dependency/BOOT-INF/classes /app

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8089 9089

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kanokna.search.SearchServiceApplication"]
