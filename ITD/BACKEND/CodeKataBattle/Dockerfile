# Step 1: Start with a JDK 21 base image
FROM eclipse-temurin:21-jdk-jammy as build
WORKDIR /app

# Manually install Maven
RUN apt-get update && \
    apt-get install -y maven

# Copy your Maven project
COPY pom.xml .
COPY src src

# Build your application
RUN mvn clean package -DskipTests

# Step 2: Run stage, also using JDK 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8443"]
