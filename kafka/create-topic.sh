#!/bin/bash

kafka-topics.sh --create --bootstrap-server "$KAFKA_PROKER_ENDPOINT" --replication-factor 1 --partitions 1 --topic "$KAFKA_TOPIC_NAME"