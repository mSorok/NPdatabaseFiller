FROM alpine:3.7


RUN apk add --virtual build-dependencies \
        build-base \
        gcc \
        wget \
        git \
    && apk add \
        bash

RUN apk update \
&& apk upgrade \
&& apk add --no-cache bash \
&& apk add --no-cache --virtual=build-dependencies unzip \
&& apk add --no-cache curl \
&& apk add --no-cache openjdk8-jre

#RUN mkdir /root/.jnati/repo/jniinchi/1.03_1/LINUX-AMD64

#COPY ./inchi/libJniInchi-1.03_1-LINUX-AMD64.so /root/.jnati/repo/jniinchi/1.03_1/LINUX-AMD64/
#COPY ./inchi/MANIFEST.xml /root/.jnati/repo/jniinchi/1.03_1/LINUX-AMD64/


EXPOSE 8080
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]




#FROM openjdk:8-jdk-alpine
#LABEL maintainer="maria.ssorokina@gmail.com"
#EXPOSE 8080
#VOLUME /tmp
#ARG JAR_FILE
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

#RUN apt-get update && \
#    apt-get upgrade -y && \
#    apt-get install -y  software-properties-common && \
#    add-apt-repository ppa:webupd8team/java -y && \
#    apt-get update && \
#    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
#    apt-get install -y oracle-java8-installer && \
#    apt-get clean
#RUN apt-get install gcc
#RUN apt-get install g++
#RUN apt-get install maven
