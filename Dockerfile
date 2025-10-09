# Use an official Gradle image to build, then a slim JRE to run
FROM gradle:8.6-jdk17 as builder
WORKDIR /work
COPY --chown=gradle:gradle . /work
# Prepare dependencies and build shadow jar
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /work/build/libs/kotlin-chess-0.1.0.jar /app/kotlin-chess.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/kotlin-chess.jar"]