# syntax=docker/dockerfile:1

# ---- Build ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# copia POM y código
COPY pom.xml .
COPY src ./src

# compilar (sin tests)
RUN mvn -q -DskipTests=true clean package

# ---- Runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# copia el jar generado
COPY --from=build /app/target/*.jar app.jar

# Render asigna PORT; Spring lo tomará por parámetro
ENV PORT=8080
EXPOSE 8080

# pasa tus props como args de línea (Spring los mapea a application.properties)
CMD ["sh","-c","java -jar app.jar \
  --server.port=${PORT} \
  --main.api.base=${MAIN_API_BASE} \
  --cors.origin=${CORS_ORIGIN} \
  --cors.origins=${CORS_ORIGINS:-} \
  --allowed.supervisor.emails=${ALLOWED_SUPERVISOR_EMAILS}"]
