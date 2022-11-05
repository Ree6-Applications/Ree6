FROM amazoncorretto:17-alpine3.16-full
MAINTAINER ree6.de
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]