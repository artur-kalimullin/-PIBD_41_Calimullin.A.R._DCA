FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY file-service/build.gradle file-service/settings.gradle ./

RUN chmod +x gradlew && \
    ./gradlew --no-daemon dependencies || true

COPY file-service/src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /app/storage

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]