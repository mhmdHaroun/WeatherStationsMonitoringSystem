FROM openjdk:19

# modify environment variable
ENV JAVA_HOME /usr/lib/jvm/java-19-openjdk-amd64
ENV KAFKA_ENDPOINT kafka-server:9092
ENV KAFKA_TOPIC_NAME weather-forecast


# Copy over the producers code
COPY station-0.0.1-SNAPSHOT-jar-with-dependencies.jar .

CMD ["java", "-jar", "station-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]
