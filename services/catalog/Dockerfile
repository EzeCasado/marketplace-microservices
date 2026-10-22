# Imagen del catálogo en tres etapas: compila con Maven, separa el jar en capas
# y lo corre sobre un JRE, sin el JDK ni el código fuente.

# ---------- 1. Compilar ----------
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /build

# Desde Windows el permiso de ejecución de mvnw no viaja, por eso el --chmod.
COPY --chmod=0755 mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./

# Primero las dependencias, con el pom solo: si no cambia, Docker reusa esta capa.
# El caché de /root/.m2 guarda también el Maven que baja el wrapper.
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B -q

# Los tests se saltean porque piden Docker para Testcontainers, y acá adentro no hay.
# Los corre el job "Catalog (build and test)" del CI.
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw package -B -q -DskipTests \
    && cp target/*.jar application.jar

# ---------- 2. Extraer las capas ----------
# Como la documentación de Spring Boot 4.1 (jarmode tools).
FROM eclipse-temurin:21-jre-noble AS extract
WORKDIR /builder
COPY --from=build /build/application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- 3. Correr ----------
FROM eclipse-temurin:21-jre-noble
WORKDIR /application

# Usuario sin root, como la guía de Java de Docker.
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

# Una capa por COPY, de la que menos cambia a la que más.
COPY --from=extract /builder/extracted/dependencies/ ./
COPY --from=extract /builder/extracted/spring-boot-loader/ ./
COPY --from=extract /builder/extracted/snapshot-dependencies/ ./
COPY --from=extract /builder/extracted/application/ ./

USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "application.jar"]
