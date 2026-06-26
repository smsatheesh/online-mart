FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY product-service/ product-service/
COPY order-service/pom.xml order-service/pom.xml
COPY cart-service/pom.xml cart-service/pom.xml

RUN mvn install -N -DskipTests
RUN mvn clean package -pl product-service -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/product-service/target/product-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]