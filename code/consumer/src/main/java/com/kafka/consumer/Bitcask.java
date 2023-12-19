package com.kafka.consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Bitcask {
	private long maxSize;
	private Map<String, String>bitcask;
	
	public Bitcask(long maxSize) {
		this.maxSize=maxSize;
		this.bitcask=new HashMap<String, String>();
	}
	  
	private byte[] readBytes(long position, RandomAccessFile file) throws Exception {
        file.seek(position);
        byte[] data = new byte[file.readShort()];
        file.readFully(data);
        return data;      
	}
    
	@SuppressWarnings("deprecation" )
	private JsonObject readJsonObject(long position, RandomAccessFile file) throws Exception {
    	byte[]data = readBytes(position, file);      
        return new JsonParser().parse(new String(data, "UTF-8")).getAsJsonObject();
    }
    
    private Map<Long, WeatherStatus> readWeatherStatus(String[]fileNames) throws Exception {
    	Map<Long, WeatherStatus> weatherStatusMap=new HashMap<Long, WeatherStatus>();
    	for(String fileName : fileNames) {  		
    		RandomAccessFile file = new RandomAccessFile(fileName, "r");
    		long position = 0;
    		while(position < file.length()) {
    			WeatherStatus weatherStatus = new Gson().fromJson(
    					readJsonObject(position, file).
    	        		entrySet().
    	        		iterator().
    	        		next().
    	        		getValue().
    	        		getAsJsonObject()  
    					, WeatherStatus.class);
    			weatherStatusMap.put(weatherStatus.getStation_id(), weatherStatus);
    			position = file.getFilePointer();
    		}
    		file.close();
        }
    	return weatherStatusMap;
    }
	
    private void readHint(String[]fileNames) throws Exception {
    	for(String fileName : fileNames) {
    		RandomAccessFile file = new RandomAccessFile(fileName, "r");
    		long position = 0;
    		while(position < file.length()) {
    			JsonObject jsonObject = readJsonObject(position, file);
    	        String key = jsonObject.entrySet().iterator().next().getKey();
    	        String value = jsonObject.entrySet().iterator().next().getValue().getAsString();
    			bitcask.put(key, value);
    			position = file.getFilePointer();
    		}
    		file.close();
        }
    }
    
    public WeatherStatus read(String key) throws Exception {
		if(!bitcask.containsKey(key)) return null;
    	String[]str = bitcask.get(key).split("_");
    	RandomAccessFile file =  new RandomAccessFile(str[0]+".bin", "r");
		WeatherStatus weatherStatus = new Gson().fromJson(
				readJsonObject(Long.valueOf(str[1]), file).
        		entrySet().
        		iterator().
        		next().
        		getValue().
        		getAsJsonObject()  
				, WeatherStatus.class);
    	file.close();
        return weatherStatus;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    
	private byte[] getBytes(String value) throws Exception {
	    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
	    DataOutputStream dataOutput = new DataOutputStream(byteOutput);
	    byte[] jsonData = value.getBytes("UTF-8");
	    dataOutput.writeShort(jsonData.length);
	    dataOutput.write(jsonData);
		return byteOutput.toByteArray();
	}
    
	//{position, index}
	private long[] getPositionIndex(String fileName, Long maxSize) throws Exception {
	    long position,fileIndex=0;
        RandomAccessFile file = new RandomAccessFile(fileName+fileIndex+".bin", "rw");
		while((position = file.length()) > maxSize) {
			file.close();
			file = new RandomAccessFile(fileName+(++fileIndex)+".bin", "rw");
		}
		file.close();
		return new long[] {position, fileIndex};	
	}
	
	private String generateWeatherStatusJson(WeatherStatus weatherStatus) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add(
				Long.toString(weatherStatus.getStation_id()),
				new Gson().toJsonTree(weatherStatus));
		return jsonObject.toString();
	}
	
	private String generateHashTableJson(String key) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(key, bitcask.get(key));
		return jsonObject.toString();
	}
    
    public void write(WeatherStatus weatherStatus, String fileName) throws Exception {
		byte[]data = getBytes(generateWeatherStatusJson(weatherStatus));
		
        long[]positionIndex = getPositionIndex(fileName, this.maxSize);
        long position = positionIndex[0];
        
        String actualFileName = fileName+positionIndex[1];

        RandomAccessFile file = new RandomAccessFile(actualFileName+".bin", "rw");
		file.seek(position);
		file.write(data);
		file.close();
		
		bitcask.put(Long.toString(weatherStatus.getStation_id()), actualFileName+"_"+position);
    }
 
    private void writeCompact(Map<Long, WeatherStatus>compact, String fileName, long maxSize) throws Exception {
		long[]positionIndex = getPositionIndex(fileName, maxSize);
		long position = positionIndex[0];

        String actualFileName = fileName+positionIndex[1];

        RandomAccessFile file = new RandomAccessFile(actualFileName+".bin", "rw");
        
    	for(Entry<Long, WeatherStatus>entry:compact.entrySet()) {
    		file.seek(position);
    		file.write(getBytes(generateWeatherStatusJson(entry.getValue())));
    		bitcask.put(Long.toString(entry.getKey()), actualFileName+"_"+position);
    		position = file.getFilePointer();
    	}
    	
    	file.close();
    }
    
	private void writeHint(Map<Long, WeatherStatus>compact, String fileName, long maxSize) throws Exception {
		long[]positionIndex = getPositionIndex(fileName, maxSize);
		long position = positionIndex[0];

        String actualFileName = fileName+positionIndex[1];

        RandomAccessFile file = new RandomAccessFile(actualFileName+".bin", "rw");
        
    	for(Entry<Long, WeatherStatus>entry:compact.entrySet()) {
    		file.seek(position);
    		file.write(getBytes(generateHashTableJson(Long.toString(entry.getKey()))));
    		position = file.getFilePointer();
    	}
    	
    	file.close();
    }
	
	public void compaction(String[] fileNames, String compactName, String hintName) throws Exception {
	    Map<Long, WeatherStatus> compact = readWeatherStatus(fileNames);
	    
	    for (String fileName : fileNames) 
	        new File(fileName).delete();

	    writeCompact(compact, compactName, 0);

	    writeHint(compact, hintName, 0);
	}
    
    public void crashRecovery(String[]fileNames) throws Exception {
    	readHint(fileNames);
    	
    	System.out.println("Build Hash Table");
    	
    	for(Entry<String, String>entry : bitcask.entrySet()) {
    		System.out.println("key : " + entry.getKey());
    		System.out.println("value : "+entry.getValue());
    	}
    }    
}