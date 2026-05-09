FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 1. Copy Gradle wrapper and root configuration
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle/libs.versions.toml gradle/

# 2. Copy subproject build files for dependency caching
COPY server/build.gradle.kts server/
COPY shared/build.gradle.kts shared/
COPY shared/android-config.gradle shared/

# 3. Pre-download dependencies (this layer is cached)
ENV SKIP_MOBILE=true
RUN chmod +x gradlew
RUN ./gradlew :server:help --no-daemon

# 4. Copy the rest of the source code
COPY . .

# 5. Build the shadowJar
RUN ./gradlew :server:shadowJar --no-daemon --info

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Note: The JAR is built in server/build/libs/
COPY --from=build /app/server/build/libs/server-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
