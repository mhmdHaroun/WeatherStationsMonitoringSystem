FROM python:3.8-slim

# Install dependencies
# Install Java (required for PySpark)
RUN apt-get update && apt-get install -y openjdk-11-jdk

# modify environment variable
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64
#ENV INPUT_PATH /data/

RUN apt-get update && apt-get install -y procps

RUN pip install pyspark elasticsearch

# Copy over the ingest python script
COPY ingest.py .

# copy source of parquet files "data"
COPY data /data

# Set default command to run the ingest script
CMD ["python", "ingest.py"]
