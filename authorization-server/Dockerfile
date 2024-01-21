FROM eclipse-temurin:17-jdk-focal AS BASE
WORKDIR /app
COPY target/authorization-server-0.0.1-SNAPSHOT.jar /app/authorization-server.jar
CMD ["java","-jar","authorization-server.jar"]