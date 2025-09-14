FROM openjdk:8-jre-slim

COPY /api/build/libs/*.jar /api.jar

ENTRYPOINT ["java", "-jar", "/api.jar"]