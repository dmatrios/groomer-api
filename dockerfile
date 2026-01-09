# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

# ===== Run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Railway / cloud suele inyectar PORT
ENV PORT=8080
EXPOSE 8080

# (opcional pero útil) zona horaria
ENV TZ=America/Lima

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
