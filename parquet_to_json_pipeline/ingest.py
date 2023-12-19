from pyspark.sql import SparkSession
from elasticsearch import Elasticsearch
import json
import os

# name of index in elastic search
index_ = os.environ.get('INDEX_NAME', 'temp')

# path of directory inside docker container of data
input_path = "/data_out/" 

# location of elasticsearch
loc = "http://172.18.0.3:9200"

# Create Spark session
spark = SparkSession.builder.appName("ParquetIngest").getOrCreate()


print("input path is : ",input_path)
# Read Parquet files 
df = spark.read.parquet(input_path + "*.parquet")

# Convert to JSON 
df_json = df.toJSON().map(lambda j: json.loads(j))
print("length of jsons : ", df_json.count())
# Send to Elasticsearch 
es = Elasticsearch([loc])
for row in df_json.collect():
    print(row)
    es.index(index=index_ , body=row)