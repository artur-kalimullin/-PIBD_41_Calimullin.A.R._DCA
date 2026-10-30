FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY merge-sort-lib/build.gradle merge-sort-lib/

RUN chmod +x gradlew && \
    ./gradlew --no-daemon dependencies || true

COPY src ./src
COPY merge-sort-lib/src merge-sort-lib/src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]