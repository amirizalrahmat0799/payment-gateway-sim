# Generic multi-stage build for any service module.
# Usage: docker build --build-arg MODULE=payment-service -t pgs/payment-service .

FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace
COPY pom.xml .
COPY common/pom.xml common/
COPY merchant-service/pom.xml merchant-service/
COPY tokenization-service/pom.xml tokenization-service/
COPY payment-service/pom.xml payment-service/
COPY settlement-service/pom.xml settlement-service/
# Cache dependencies in their own layer
RUN mvn -B -q -pl ${MODULE} -am dependency:go-offline -DskipTests || true
COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests \
    && cp ${MODULE}/target/${MODULE}-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine
# Links images pushed to GHCR to this repository
LABEL org.opencontainers.image.source=https://github.com/amirizalrahmat0799/payment-gateway-sim
# Fixed numeric UID so Kubernetes can verify runAsNonRoot
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app
USER 10001
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
