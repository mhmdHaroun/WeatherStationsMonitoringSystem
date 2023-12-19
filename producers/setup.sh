#!/bin/bash

echo "Enter image name:"
read IMAGE_NAME

echo "Enter kafka endpoint:"
read KAFKA_ENDPOINT

echo "Enter kafka topic name:"
read KAFKA_TOPIC_NAME

docker build -t "producers" .
docker run -e KAFKA_ENDPOINT="$KAFKA_ENDPOINT" -e KAFKA_TOPIC_NAME="$KAFKA_TOPIC_NAME" --network app-tier --name "$IMAGE_NAME" producers
