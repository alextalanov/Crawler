![Scala CI](https://github.com/alextalanov/Crawler/workflows/Scala%20CI/badge.svg?branch=master&event=push)

## Create input topic

docker exec -it kafka1 bash

cd kafka_2.12-2.4.0/

bin/kafka-topics.sh --create \
    --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic countries

## Create output topic

docker exec -it kafka1 bash

cd kafka_2.12-2.4.0/

bin/kafka-topics.sh --create \
    --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic countries-review \
    --config cleanup.policy=compact

## Check created topics

docker exec -it kafka1 bash

cd kafka_2.12-2.4.0/

bin/kafka-topics.sh --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --describe

## Command to run Crawler app in sbt shell

docker container attach crawler

cd ../crawler

sbt

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -c 2 -f /output/csv/data.csv -v

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -c 2 -m "parquet" -f /output/parquet/data.parquet -v

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -c 1 -m "kafka"

## Command to run Spark History Server

docker exec -it spark_driver bash

$SPARK_HOME/sbin/start-history-server.sh

## Create HBase table

docker exec -it hbase_master bash

hbase shell

create "CountriesReview", "A"

## Command to run Spark Job

docker exec -it spark_driver bash

$SPARK_HOME/bin/spark-submit \
    --class com.gmail.wristylotus.jobs.spark.CountriesReviewSqlStreamJob \
    --packages org.apache.spark:spark-streaming-kafka-0-10_2.11:2.4.0,org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 \
    --master yarn --deploy-mode client \
    --driver-memory 4g \
    --executor-memory 2g \
    --executor-cores 2 \
    --num-executors=2 \
    --conf spark.yarn.am.memory=512m \
    --conf spark.scheduler.mode=FAIR \
    --queue spark_app \
    $SPARK_DRIVER_JAR_PATH/CountriesReview-assembly-0.1.jar \
    -d 5 \
    -c "hdfs://namenode:9000/spark/jobs/CountriesReviewSqlStreamJob/checkpoint" \
    -p "kafka-config.properties"

## Command to run Kafka Stream Job

docker exec -it spark_driver bash

cd app/

sbt

runMain com.gmail.wristylotus.jobs.kafka.CountriesReviewStreamJob -p "kafka-config.properties"

bin/kafka-console-consumer.sh \
      --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic countries-review \
      --from-beginning --formatter kafka.tools.DefaultMessageFormatter \
      --property print.key=true --property print.value=true \
      --property key.deserialzer=org.apache.kafka.common.serialization.StringDeserializer \
      --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
      --from-beginning

## Command to run PySpark Job

docker exec -it py_spark_driver bash

cd app/

If it first time run:
  make init

source `pipenv --venv`/bin/activate

make run JOB_NAME=counties_review CONFIG_PATH="$(pwd)/src/resources/default_config.json"

