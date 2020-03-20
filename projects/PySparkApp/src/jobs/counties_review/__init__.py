from pyspark.sql import SparkSession
from pyspark import SparkContext


def run(spark: SparkSession, config: dict):
    print(config["in_topic"])
    print(sp(spark.sparkContext, data=[1, 2, 3, 4, 5]))


def sp(context: SparkContext, data):
    return context.parallelize(data) \
        .map(lambda n: n + 1) \
        .reduce(lambda l, r: l + r)
