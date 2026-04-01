FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .. .
RUN ./gradlew :eureka:bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre
COPY --from=build /app/eureka/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
