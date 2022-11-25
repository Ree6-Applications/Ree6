MAINTAINER ree6.de

#
# Build the image
#
FROM maven:3.8.6-eclipse-temurin-17-alpine
RUN mvn -f pom.xml clean package

#
# Package the image
#
FROM amazoncorretto:17-alpine3.16-full
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY ${JAR_FILE} Ree6.jar
ENTRYPOINT ["java","-jar","/Ree6.jar"]