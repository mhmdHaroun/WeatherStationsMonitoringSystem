package com.mock.station;

import org.json.JSONObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.*;

public class WeatherStationMock {
	private String stationID;
	private final static long toSleep = 950;

	WeatherStationMock(String stationID) {
		this.stationID = stationID;
	}

	public void run() {
		String[] x = this.stationID.split("-");
		String y = x[x.length-1];
		WeatherMessageGenerator gen = new WeatherMessageGenerator(Long.parseLong(y)+1);
		//WeatherMessageGenerator gen = new WeatherMessageGenerator(Long.parseLong(this.stationID));
		KafkaProducer<String, String> producer = new KafkaProducer<>(App.getProperties());
		while (true) {
			JSONObject jsonMessage = gen.getRandomJsonMessage(Math.random() < 0.1);
			ProducerRecord<String, String> record = new ProducerRecord<>(App.topicName, jsonMessage.toString());
			System.out.println("\n\n" + jsonMessage.toString() + "\n\nsent to : " + App.bootStrap);
			producer.send(record);
			try {
				Thread.sleep(toSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


}
