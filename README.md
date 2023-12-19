# WeatherStationSystem


   ****weather station system**** 

This system facilitates the collection and processing of weather-related data through a distributed architecture. Below are the key components and instructions for running the project.

  **Producers Configuration**
KAFKA_ENDPOINT: The Kafka server endpoint. Default value: kafka-server:9092.
KAFKA_TOPIC_NAME: The Kafka topic name for weather forecasts. Default value: weather-forecast.

  **Consumers Configuration**
KAFKA_ENDPOINT: The Kafka server endpoint. Default value: kafka-server:9092.
KAFKA_TOPIC_NAME: The Kafka topic name for weather forecasts. Default value: weather-forecast.
ELASTIC_INPUT: The ElasticSearch server endpoint for input. Default value: http://172.18.0.3:9200.
ELASTIC_INDEX: The ElasticSearch index name. Default value: idx.
OUTPUT_FOLDER: The folder path for data output. Default value: /data_out.
BATCH_SIZE: The batch size for data processing. Default value: 10000.
WRITING_TIME_RANGE: The time range for data writing. Default value: 5.
ElasticSearch Configuration
ELASTICSEARCH_INDEX: The ElasticSearch index name. Default value: idx.

  
  
  
**how to run this project**
To run this project, follow the steps below:
  

Run the producers container:
docker run -e KAFKA_ENDPOINT="kafka-server:9092" -e KAFKA_TOPIC_NAME="weather-forecast" -e ID="aaa-3" --network app-tier --name "weather-stations" producers
	
Run the ElasticSearch and Kibana container:
docker run -d -p 9200:9200 -p 5601:5601 --network app-tier -e ELASTICSEARCH_INDEX="idx" --name "elastic-server" nshou/elasticsearch-kibana

Run the consumers container:

docker run --network app-tier -e BATCH_SIZE="10" -e WRITING_TIME_RANGE="5" -e KAFKA_ENDPOINT="kafka-server:9092" -e KAFKA_TOPIC_NAME="weather-forecast" -e OUTPUT_FOLDER="/data_out" -e ELASTIC_INPUT="http://172.18.0.3:9200" -e ELASTIC_INDEX="idx" --name "consumers" --mount source=out,target=/data_out central









