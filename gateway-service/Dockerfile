FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/gateway-service-0.0.1-SNAPSHOT.jar /app/gateway-service.jar
CMD ["java","-jar","gateway-service.jar"]