FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :server:shadowJar --no-daemon --info

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/server/build/libs/server-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
