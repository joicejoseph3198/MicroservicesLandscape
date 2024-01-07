FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/discovery-service-0.0.1-SNAPSHOT.jar /app/discovery-service.jar
CMD ["java","-jar","discovery-service.jar"]