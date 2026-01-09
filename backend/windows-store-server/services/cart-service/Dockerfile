# Multi-stage Dockerfile for cart-service
# Build stage
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY services/cart-service/pom.xml services/cart-service/

# Download dependencies
RUN ./mvnw dependency:go-offline -B -pl services/cart-service

# Copy source code
COPY services/cart-service/src services/cart-service/src

# Build application
RUN ./mvnw package -DskipTests -pl services/cart-service
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

# Add non-root user
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy artifacts from build stage
COPY --from=build /workspace/app/services/cart-service/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /workspace/app/services/cart-service/target/dependency/META-INF /app/META-INF
COPY --from=build /workspace/app/services/cart-service/target/dependency/BOOT-INF/classes /app

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8083 9083

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kanokna.cart.CartServiceApplication"]
