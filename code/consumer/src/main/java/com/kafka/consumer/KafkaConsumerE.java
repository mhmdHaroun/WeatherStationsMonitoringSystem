package com.kafka.consumer;

import java.util.Properties;


import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import org.apache.spark.sql.SparkSession;


import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.kafka.common.TopicPartition;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class KafkaConsumerE {


	private static String bootStrap = "kafka-server:9092";
	private static String topicName = "weather-forecast";
	private static String groupId = "Consumer-Group-1";
	private static String outputRepository = "//data_out";
	private static String elasticInput = "http://172.18.0.3:9200";
	private static String elasticIndex = "idx";
	private static String batchSize = "10000";
	private static String WRITING_TIME_RANGE = "5";

	private static String produceTopicName = "will-rain";
	
	static SparkSession spark = SparkSession.builder()
            .appName("JsonToParquetConverter")
            .master("local[*]") // Use local mode for simplicity
            .getOrCreate();

	private static List<WeatherStatus> messages = new ArrayList<>();
	
	public static Properties getConsumerProperties() {
	    Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootStrap);
        consumerProps.put("group.id", groupId);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);
        return consumerProps;
		}
	    
	    public static Properties getProducerProperties() {
	    	Properties producerProps = new Properties();
	        producerProps.put("bootstrap.servers", bootStrap);
	        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	        return producerProps;
	    }

    public static void main(String[] args) throws Exception {
    	bootStrap = System.getenv("KAFKA_ENDPOINT");
    	topicName = System.getenv("KAFKA_TOPIC_NAME");
    	outputRepository = System.getenv("OUTPUT_FOLDER");
    	elasticInput = System.getenv("ELASTIC_INPUT");
    	elasticIndex = System.getenv("ELASTIC_INDEX");
    	batchSize = System.getenv("BATCH_SIZE");
    	WRITING_TIME_RANGE = System.getenv("WRITING_TIME_RANGE");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(getConsumerProperties());
        TopicPartition topicPartition = new TopicPartition(topicName, 0);
		consumer.assign(Collections.singleton(topicPartition));

		// Seek to the beginning of the partition
		consumer.seekToBeginning(Collections.singleton(topicPartition));


        try (KafkaProducer<String, String> producer = new KafkaProducer<>(getProducerProperties())) {
			try {
				String schemaString = "{\r\n"
		                + "  \"type\": \"record\",\r\n"
		                + "  \"name\": \"WeatherStatus\",\r\n"
		                + "  \"fields\": [\r\n"
		                + "    {\"name\": \"station_id\", \"type\": \"long\"},\r\n"
		                + "    {\"name\": \"s_no\", \"type\": \"long\"},\r\n"
		                + "    {\"name\": \"battery_status\", \"type\": [\"null\", \"string\"]},\r\n"
		                + "    {\"name\": \"status_timestamp\", \"type\": \"long\"},\r\n"
		                + "    {\r\n"
		                + "      \"name\": \"weather\",\r\n"
		                + "      \"type\": {\r\n"
		                + "        \"type\": \"record\",\r\n"
		                + "        \"name\": \"Weather\",\r\n"
		                + "        \"fields\": [\r\n"
		                + "          {\"name\": \"humidity\", \"type\": [\"null\", \"int\"]},\r\n"
		                + "          {\"name\": \"temperature\", \"type\": [\"null\", \"int\"]},\r\n"
		                + "          {\"name\": \"wind_speed\", \"type\": [\"null\", \"int\"]}\r\n"
		                + "        ]\r\n"
		                + "      }\r\n"
		                + "    }\r\n"
		                + "  ]\r\n"
		                + "}";
				Schema.Parser parser = new Schema.Parser();
				Schema schema = parser.parse(schemaString);
				
				BitcaskProxy bitcask = new BitcaskProxy(2000);			
				
				bitcask.crashRecovery(0);
				bitcask.compact(5);
				
				for(int i = 1; i <= 10; i++)
					bitcask.read(1, String.valueOf(i));
				
			    while (true) {
			        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			        Gson gson = new Gson();
			
			        for (ConsumerRecord<String, String> record : records) {
			        	
			        	String value = record.value();
				        WeatherStatus weatherStatus = gson.fromJson(value, WeatherStatus.class);
				        
			            //System.out.println("Received message: " + value);
			          
				        if(weatherStatus.getStation_id() != 0) {
					        bitcask.write(weatherStatus);	
					        if(weatherStatus.getWeather() != null &&  weatherStatus.getWeather().getHumidity() >= 70) {
		                        ProducerRecord<String, String> ms = new ProducerRecord<>(produceTopicName, record.value());
		                        producer.send(ms);
					        }
				        }

				        //writingToParquet(weatherStatus, 100);
				        //writingToParquet(weatherStatus, 5, schema);
				        writingToParquet(weatherStatus, Integer.parseInt(batchSize), Integer.parseInt(WRITING_TIME_RANGE), schema);
				        sendToElastic(weatherStatus);
			        }
			    }
			} finally {
			    consumer.close();
			}
		} catch (JsonSyntaxException | NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    @SuppressWarnings("deprecation")
	public static void sendToElastic(WeatherStatus message) {
    	String messageJSON = new Gson().toJson(message);
    	String url = elasticInput+"//" + elasticIndex + "//_doc"; // Elasticsearch URL
    	
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        
        // Set the JSON content type and body
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
			httpPost.setEntity(new StringEntity(messageJSON));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        // Execute the request
        try {
            HttpResponse response = httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
        
       
        

    }
    
    
    
    @SuppressWarnings("deprecation")
    public static void writingToParquet(WeatherStatus msg, long maxMessages, long range,Schema schema) {
        messages.add(msg);
        File folder = new File(outputRepository);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                System.out.println("Folder created successfully");
            } else {
                return;
            }
        };
        if (messages.size() == maxMessages) {
        	Map<String, List<WeatherStatus>> partitions = partition(range);
            for (Entry<String, List<WeatherStatus>> partition : partitions.entrySet()) {
            	String[]idTimeStamp = partition.getKey().split("_");
            	File idDirectory = new File(outputRepository + "//" + idTimeStamp[0]);
            	File timestampDirectory = new File(outputRepository + "//" + idTimeStamp[0]+"//"+idTimeStamp[1]);
            	if(!idDirectory.exists())
            		idDirectory.mkdirs();
            	if(!timestampDirectory.exists())
            		timestampDirectory.mkdir();
            	
            	List<WeatherStatus> list = partition.getValue();
            	for(WeatherStatus message : list) {
                    try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
                            new org.apache.hadoop.fs.Path(outputRepository + "//" + idTimeStamp[0] + "//" +idTimeStamp[1] + "//" +message.getStation_id() + "_" + message.getStatus_timestamp() + ".parquet"))
                            .withSchema(schema)
                            .withCompressionCodec(CompressionCodecName.SNAPPY)
                            .withWriteMode(ParquetFileWriter.Mode.CREATE)
                            .build()) {

                        GenericRecord record = new GenericData.Record(schema);

                        record.put("station_id", message.getStation_id());
                        record.put("s_no", message.getS_no());                   
                        record.put("status_timestamp", message.getStatus_timestamp());
                        
                        GenericRecord weatherRecord = new GenericData.Record(schema.getField("weather").schema());
                        
                        if(message.getStation_id()!=0) {
                        	record.put("battery_status", message.getBattery_status());
                        	                      
                            weatherRecord.put("humidity", message.getWeather().getHumidity());
                            weatherRecord.put("temperature", message.getWeather().getTemperature());
                            weatherRecord.put("wind_speed", message.getWeather().getWind_speed());
                        }
                        record.put("weather", weatherRecord);
                        
                        writer.write(record);
                        writer.close();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
            	}

            }
            messages.clear();
        }
    }
    
    private static Map<String, List<WeatherStatus>> partition(long range) {
    	Map <String, List<WeatherStatus>> partitions = new HashMap<>();
    	for(WeatherStatus message : messages) {
    		long stationId = message.getStation_id();
    		long timestamp = message.getStatus_timestamp();
    		long partition = timestamp/range;
    		String key = stationId+"_"+partition;
            if(!partitions.containsKey(key)) 
            	partitions.put(key, new ArrayList<WeatherStatus>());                 	
            partitions.get(key).add(message);
    	}
    	return partitions;	
    }

    
}
