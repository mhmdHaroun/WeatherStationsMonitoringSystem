#!/bin/bash

echo "Enter image name:"
read IMAGE_NAME

docker build -t "producers" .
docker run --network app-tier --name "$IMAGE_NAME" producers
