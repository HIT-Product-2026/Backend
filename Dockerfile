# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first to download dependencies (helps with Docker caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files and package the jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/lockly1-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
