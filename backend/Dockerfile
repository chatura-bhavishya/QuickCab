# --- Build stage: compile the jar with the Maven wrapper ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Download dependencies first so they're cached between code changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# --- Run stage: just a JRE and the jar, running as a non-root user ---
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 quickcab
COPY --from=build /app/target/cabapp-*.jar app.jar
USER quickcab

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
