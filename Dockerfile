FROM openjdk:8u171-slim
EXPOSE 8080
VOLUME /tmp
#ARG JAR_FILE
#COPY ${JAR_FILE} app.jar
COPY target/npdatabasefiller-0.0.1-SNAPSHOT.jar app.jar
COPY target/inchiPet.jar /

RUN java -jar inchiPet.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

