package com.mock.station;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class App {
	public static String bootStrap = "localhost:9092";
	public static String topicName = "weather-forecast";
	public static String ID = "-1";
	
	public static void launch() {
		WeatherStationMock mockW = new WeatherStationMock(ID);
		mockW.run();
	}
	
	public static final Properties getProperties() {
		Properties producerProps = new Properties();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrap);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return producerProps;
	}

	public static void main(String[] args) {
		bootStrap = System.getenv("KAFKA_ENDPOINT");
		topicName = System.getenv("KAFKA_TOPIC_NAME");
		ID = System.getenv("ID");
		launch();
	}
}
