package com.kafka.consumer;

public class WeatherStatus {
	private long station_id;
	private long s_no;
	private String battery_status;
	private long status_timestamp;
	private Weather weather;
	
    @Override
    public String toString() {
    	return "station_id : "+station_id+
    			"\ns_no : "+s_no+
    			"\nbattery_status : "+battery_status+
    			"\nstatus_timestamp : "+status_timestamp+
    			"\nWeather : \n"+weather;
    }
    
	public long getStation_id() {
		return station_id;
	}

	public long getS_no() {
		return s_no;
	}

	public String getBattery_status() {
		return battery_status;
	}

	public long getStatus_timestamp() {
		return status_timestamp;
	}

	public Weather getWeather() {
		return weather;
	}
}
