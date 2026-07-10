FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom first to leverage Docker layer cache for dependencies
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# Copy source and build
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]