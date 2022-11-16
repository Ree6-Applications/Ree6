FROM amazoncorretto:17-alpine3.16-full
MAINTAINER ree6.de
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY ${JAR_FILE} Ree6.jar
ENTRYPOINT ["java","-jar","/Ree6.jar"]