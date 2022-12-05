FROM openjdk:19-ea-jdk-slim-bullseye as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw -Dtest=\!*RedisIntegrationTest install

FROM openjdk:19-ea-jdk-slim-bullseye
WORKDIR usr/app
VOLUME /tmp
ENV TARGET_FOLDER=/workspace/app/target
ENV JAR_FILE=fetch-0.0.1-SNAPSHOT.jar
COPY --from=build ${TARGET_FOLDER}/${JAR_FILE} /usr/app

ENTRYPOINT java -Dspring.profiles.active=docker -jar ${JAR_FILE}