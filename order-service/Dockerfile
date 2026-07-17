FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/ order-service/
COPY cart-service/pom.xml cart-service/pom.xml
COPY customer-service/pom.xml customer-service/pom.xml

RUN mvn install -N -DskipTests
RUN mvn clean package -pl order-service -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/order-service/target/order-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]