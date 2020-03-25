from pathlib import Path
from pyspark.sql import SparkSession, DataFrame, DataFrameReader


def run(spark: SparkSession, config: dict):
    countries_coords = load_csv(
        spark=spark,
        path=Path.cwd() / config['countries.coords']
    )
    countries_coords.createOrReplaceTempView('countries_coordinates')

    stream = build_kafka_stream(
        stream=spark.readStream,
        config=config
    )

    country_review = stream.selectExpr(
        'CAST(key as STRING) as country', 'CAST(value as STRING) as score')

    country_review.createOrReplaceTempView('countries_review')

    country_review_coords = spark.sql('''
        SELECT /*+ BROADCAST(coords) */
           rv.country         AS country,
           rv.score           AS score,
           coords.longitude   AS longitude,
           coords.latitude    AS latitude
        FROM countries_review rv
        JOIN countries_coordinates coords
           ON coords.country = rv.country
        ''')

    query = country_review_coords.writeStream \
        .outputMode('update') \
        .format('console') \
        .option('checkpointLocation', config['checkpoint.location']) \
        .trigger(processingTime=config['batch.duration']) \
        .start()

    query.awaitTermination()


def load_csv(spark: SparkSession, path: Path) -> DataFrame:
    return spark.read.csv(
        path=str(path),
        header=True,
        inferSchema=True
    )


def build_kafka_stream(stream: DataFrameReader, config: dict) -> DataFrame:
    return stream \
        .format('kafka') \
        .option('subscribe', config['topic']) \
        .option('startingOffsets', config['auto.offset.reset']) \
        .option('kafka.bootstrap.servers', config['bootstrap.servers']) \
        .load()
