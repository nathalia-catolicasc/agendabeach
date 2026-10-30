# ---------- Etapa 1: build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia só o pom.xml primeiro para cachear as dependências
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# curl é usado só pelo HEALTHCHECK
RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/* \
	&& groupadd --gid 10001 app \
	&& useradd --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build /app/target/agenda-beach-*.jar app.jar

USER app:app

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
	CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
