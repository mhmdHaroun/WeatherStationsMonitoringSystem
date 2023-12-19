package com.kafka.consumer;

public class Weather {
	private int humidity;
	private int temperature;
	private int wind_speed;
	
    @Override
    public String toString() {
    	return "\thumidity : "+humidity+
    			"\n\ttemperature : "+temperature+
    			"\n\twind_speed : "+wind_speed;
    }
    
	public int getHumidity() {
		return humidity;
	}

	public int getTemperature() {
		return temperature;
	}

	public int getWind_speed() {
		return wind_speed;
	}

}
