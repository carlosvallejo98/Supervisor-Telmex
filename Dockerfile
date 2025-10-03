# ---- Etapa 1: build (Maven) ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

# Copiamos el proyecto de Spring Boot
COPY supervisor-backend/pom.xml .
COPY supervisor-backend/src ./src

# Compilamos y generamos el JAR (sin tests)
RUN mvn -B -DskipTests clean package

# ---- Etapa 2: run (JRE) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el JAR construido
COPY --from=build /build/target/*.jar /app/app.jar

# (Opcional) tuning de memoria
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75"

# Render te inyecta PORT; Spring debe escuchar ahí
# Pasamos los props como args para sobreescribir application.properties
CMD sh -c 'java $JAVA_OPTS -jar app.jar \
  --server.port=${PORT:-3003} \
  --main.api.base=${MAIN_API_BASE} \
  --cors.origin=${CORS_ORIGIN} \
  --cors.origins=${CORS_ORIGINS} \
  --allowed.supervisor.emails=${ALLOWED_SUPERVISOR_EMAILS}'
