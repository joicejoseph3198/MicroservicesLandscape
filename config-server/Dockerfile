FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/config-server-0.0.1-SNAPSHOT.jar /app/config-server.jar
CMD ["java","-jar","config-server.jar"]