## Command to run Crawler app in sbt shell

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -a "hdfs://namenode:9000/" -c 2 -f /crawler/output/csv/data.csv

runMain com.gmail.wristylotus.CrawlerApp -q travel-dataset.txt -a "hdfs://namenode:9000/" -c 2 -m "parquet" -f /crawler/output/parquet/data.parquet

## Command to run Spark History Server

$SPARK_HOME/sbin/start-history-server.sh

## Command to run Spark Job

$SPARK_HOME/bin/spark-submit \
    --class com.gmail.wristylotus.Main \
    --master yarn --deploy-mode client \
    --driver-memory 4g \
    --executor-memory 2g \
    --executor-cores 2 \
    --num-executors=2 \
    --conf spark.yarn.am.memory=512m \
    --conf spark.scheduler.mode=FAIR \
    --queue spark_app \
    $SPARK_DRIVER_JAR_PATH/CountriesReview-assembly-0.1.jar \
    -a "hdfs://namenode:9000/" \
    -i "crawler/output/parquet/data.parquet_*" \
    -o "spark/output/parquet"