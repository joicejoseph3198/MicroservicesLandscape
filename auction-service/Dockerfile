FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/auction-service-0.0.1-SNAPSHOT.jar /app/auction-service.jar
CMD ["java","-jar","auction-service.jar"]