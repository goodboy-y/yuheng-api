FROM ubuntu/jdk:21-24.04
COPY target/yuheng-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8520
ENTRYPOINT ["java", "-jar", "/app.jar"]