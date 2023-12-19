#!/bin/bash

echo "Enter image name:"
read IMAGE_NAME
echo "Enter index name:"
read INDEX_NAME

docker build -t "pyspark" .
docker run -e INDEX_NAME="idx" --network app-tier --name "par" --mount source=out,target=/data_out pyspark
