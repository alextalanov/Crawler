## Command to run Crawler app in sbt shell

docker exec -it crawler bash

cd ../crawler

sbt

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -a "hdfs://namenode:9000/" -c 2 -f /crawler/output/csv/data.csv

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -a "hdfs://namenode:9000/" -c 2 -m "parquet" -f /crawler/output/parquet/data.parquet

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
    --class com.gmail.wristylotus.jobs.spark.CountriesReviewStreamJob \
    --master yarn --deploy-mode client \
    --driver-memory 4g \
    --executor-memory 2g \
    --executor-cores 2 \
    --num-executors=2 \
    --conf spark.yarn.am.memory=512m \
    --conf spark.scheduler.mode=FAIR \
    --queue spark_app \
    $SPARK_DRIVER_JAR_PATH/CountriesReview-assembly-0.1.jar \
    -d 1 \
    -i 1 \
    -c "hdfs://namenode:9000/spark/checkpoint/jobs/CountriesReview" \
    -p "kafka-config.properties"

