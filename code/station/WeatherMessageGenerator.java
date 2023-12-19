package com.mock.station;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WeatherMessageGenerator {
	// Define the battery status percentages
    private final static Map<String, Double> BATTERY_STATUS_PERCENTAGES = new HashMap<String, Double>();
    static {
        BATTERY_STATUS_PERCENTAGES.put("low", 0.3);
        BATTERY_STATUS_PERCENTAGES.put("medium", 0.4);
        BATTERY_STATUS_PERCENTAGES.put("high", 0.3);
    }
	private static final Random random = new Random();
	private long seqNo = 1, stationID;

	WeatherMessageGenerator(long stationID) {
		this.stationID = stationID;
	}
	
	private String chooseRandomBatteryStatus() {
        double value = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (Map.Entry<String, Double> entry : BATTERY_STATUS_PERCENTAGES.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (value <= cumulativeProbability) 
                return entry.getKey();
        }
        return "medium"; // fallback option
    }

	public JSONObject getRandomJsonMessage(boolean isDropped) {
		JSONObject weatherMessage = new JSONObject();
		if (isDropped) {
			weatherMessage.put("status_timestamp", System.currentTimeMillis() / 1000L).put("s_no", seqNo++);
		} else {
			JSONObject weather = new JSONObject();
			weather.put("humidity", random.nextInt(100)).put("temperature", random.nextInt(150)).put("wind_speed",
					random.nextInt(100));
			weatherMessage.put("station_id", stationID).put("s_no", seqNo++)
					.put("battery_status", chooseRandomBatteryStatus())
					.put("status_timestamp", System.currentTimeMillis() / 1000L).put("weather", weather);
		}
		return weatherMessage;
	}
	
}
