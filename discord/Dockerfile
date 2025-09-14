FROM openjdk:8-jre-slim

COPY /discord/build/libs/*-all.jar /discord.jar

ENTRYPOINT ["java", "-jar", "/discord.jar"]