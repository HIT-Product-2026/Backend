FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom trước để tận dụng cache dependency
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# Copy source và các file cấu hình Maven
COPY src ./src
# Nếu có Maven Wrapper thì thêm:
# COPY .mvn .mvn
# COPY mvnw .

# Nếu có các thư mục resource khác ngoài src thì copy tương ứng

# Build
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]