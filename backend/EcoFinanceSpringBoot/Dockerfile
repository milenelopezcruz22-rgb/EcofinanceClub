# Dockerfile para desplegar el backend Spring Boot (EcoFinance) en Railway.
# No modifica el código ni el pom.xml existentes: solo define cómo compilar
# y ejecutar el mismo proyecto.

# ---- Etapa 1: build con Maven + JDK 21 ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos primero el wrapper de Maven y el pom para aprovechar la cache de
# dependencias en builds sucesivos.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

# Copiamos el resto del código fuente y compilamos el jar (sin tests, para
# acelerar el build en Railway; los tests ya se corren en desarrollo local).
COPY src src
RUN ./mvnw -q clean package -DskipTests

# ---- Etapa 2: imagen final, solo JRE + el jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/ecofinance-0.0.1-SNAPSHOT.jar app.jar

# Railway inyecta la variable PORT en tiempo de ejecución; server.port ya
# está mapeado a ${PORT:8080} en application.properties / application-prod.properties.
EXPOSE 8080

# Activamos el perfil "prod" (application-prod.properties) para que se usen
# las variables de entorno de Railway en vez de los valores de localhost.
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
