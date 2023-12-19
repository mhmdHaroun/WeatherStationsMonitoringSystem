#!/bin/bash

echo "Enter image name:"
read IMAGE_NAME
echo "Enter index name:"
read INDEX_NAME

docker run -d -p 9200:9200 -p 5601:5601 --network app-tier -e ELASTICSEARCH_INDEX="$INDEX_NAME" --name "$IMAGE_NAME" nshou/elasticsearch-kibana

curl -X PUT "localhost:9200/_ilm/policy/my-lifecycle-policy?pretty" -H "Content-Type: application/json" -d"{  \"policy\": \"phases\": {      \"hot\": {        \"actions\": {          \"rollover\": {            \"max_size\": \"50GB\"          }        }      },      \"delete\": {        \"min_age\": \"60d\",        \"actions\": {          \"delete\": {}        }      }    }  }}"


curl -X PUT "localhost:9200/_index_template/$INDEX_NAME?pretty" -H "Content-Type: application/json" -d"{  \"index_patterns\": [ \"*\" ],   \"priority\": 500,  \"template\": {\"settings\": {      \"index.lifecycle.name\": \"my-policy-delete\"},\"mappings\":{      \"properties\": {}}  }}"

