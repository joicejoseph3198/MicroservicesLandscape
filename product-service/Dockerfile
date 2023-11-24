FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/product-service-0.0.1-SNAPSHOT.jar /app/product-service.jar
CMD ["java","-jar","product-service.jar"]