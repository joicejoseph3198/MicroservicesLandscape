FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/product-composite-service-0.0.1-SNAPSHOT.jar /app/product-composite-service.jar
CMD ["java","-jar","product-composite-service.jar"]