FROM eclipse-temurin:21-jdk AS BASE
WORKDIR /app
COPY target/auction-consumer-0.0.1-SNAPSHOT.jar /app/auction-consumer.jar
CMD ["java", "-jar", "auction-consumer.jar"]
