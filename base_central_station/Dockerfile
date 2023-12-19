FROM openjdk:19

# modify environment variable
ENV JAVA_HOME /usr/lib/jvm/java-19-openjdk-amd64
ENV KAFKA_ENDPOINT kafka-server:9092
ENV KAFKA_TOPIC_NAME weather-forecast
ENV ELASTIC_INPUT http://172.18.0.3:9200
ENV ELASTIC_INDEX idx
ENV OUTPUT_FOLDER /data_out


# Copy over the producers code
COPY consumer-0.0.1-SNAPSHOT-jar-with-dependencies.jar .

CMD ["java", "-jar", "consumer-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]
