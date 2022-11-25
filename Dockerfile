# syntax=docker/dockerfile:1

MAINTAINER ree6.de

FROM maven:3.8.6-amazoncorretto-17 AS build
RUN mvn -B package --file pom.xml

FROM amazoncorretto:17-alpine3.16-full
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY --from=build ${JAR_FILE} Ree6.jar
ENTRYPOINT ["java","-jar","/Ree6.jar"]