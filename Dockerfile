FROM node:20 AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build


FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]