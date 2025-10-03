# syntax=docker/dockerfile:1

# ---- Build ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos POM y código
COPY pom.xml .
COPY src ./src

# Compilar y reempacar Boot Jar (fat jar)
RUN mvn -q -DskipTests=true clean package spring-boot:repackage

# ---- Runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el jar resultante y lo nombramos app.jar
COPY --from=build /app/target/*.jar app.jar

# Render setea PORT -> se lo pasamos a Spring
ENV PORT=8080
EXPOSE 8080

# Propiedades que tu app lee (las pones como Env Vars en Render)
# MAIN_API_BASE, CORS_ORIGIN, CORS_ORIGINS, ALLOWED_SUPERVISOR_EMAILS
CMD ["sh","-c","java -jar app.jar \
  --server.port=${PORT} \
  --main.api.base=${MAIN_API_BASE} \
  --cors.origin=${CORS_ORIGIN} \
  --cors.origins=${CORS_ORIGINS:-} \
  --allowed.supervisor.emails=${ALLOWED_SUPERVISOR_EMAILS}"]
