# Usamos Java 17
FROM eclipse-temurin:17-jdk

# Creamos un directorio dentro del contenedor
WORKDIR /app

# Copiamos Maven wrapper y pom.xml primero (para cachear dependencias)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargamos dependencias
RUN ./mvnw dependency:go-offline -B

# Copiamos el resto del código fuente
COPY src src

# Construimos el .jar (saltamos tests para más rápido)
RUN ./mvnw clean package -DskipTests

# Render pone el puerto en la variable $PORT
EXPOSE 8080

# Arrancamos la app con el .jar que se generó en /target
CMD ["java", "-jar", "target/*.jar"]
