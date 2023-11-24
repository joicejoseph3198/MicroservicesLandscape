FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/review-service-0.0.1-SNAPSHOT.jar /app/review-service.jar
CMD ["java","-jar","review-service.jar"]