# --- build ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
# sed remove CRLF caso o mvnw venha do Windows (core.autocrlf)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q package -DskipTests

# --- runtime ---
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
