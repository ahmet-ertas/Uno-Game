FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw clean package
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
