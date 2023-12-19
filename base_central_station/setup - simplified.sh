#!/bin/bash

echo "Enter image name:"
read IMAGE_NAME

docker build -t "central" .
docker run --network app-tier --name "consumers" --mount source=out,target=/data_out central
