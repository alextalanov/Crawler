package com.gmail.wristylotus.jobs.spark

import com.gmail.wristylotus.jobs.configuration.KafkaConfiguration
import com.gmail.wristylotus.jobs.model.HtmlPage
import com.gmail.wristylotus.kafka.SearchContentDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.streaming.DataStreamReader
import org.apache.spark.sql.streaming.Trigger.ProcessingTime
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import scala.concurrent.duration._

class CountriesReviewSqlStreamJob(sparkSession: SparkSession) {

  private implicit val spark = sparkSession

  import spark.implicits._

  def run(args: Seq[String]): Unit = {

    val config = KafkaConfiguration(args)

    val stream = prepareKafkaStream(spark.readStream, config).load()

    val deserializer = spark.sparkContext.broadcast(SearchContentDeserializer())

    val htmlPageDs = stream
      .selectExpr("CAST(key as STRING)", "CAST(value as STRING)")
      .as[(String, String)]
      .map { case (_, value) => deserializer.value.deserialize(value) }

    transformToQueryAndWord(htmlPageDs)
      .toDF("query", "word")
      .createOrReplaceTempView("CountriesReview")

    spark.createDataset(goodMarkers.toSeq)
      .toDF("word")
      .createOrReplaceTempView("goodMarkers")

    val queryScoreDF = evalQueryScore(spark)

    val countriesReviewQuery = queryScoreDF.writeStream
      .outputMode("update")
      .format("console")
      .option("checkpointLocation", config.checkpointDir())
      .trigger(ProcessingTime(config.batchDuration().seconds))
      .start()

    countriesReviewQuery.awaitTermination()
  }

  private def prepareKafkaStream(stream: DataStreamReader, config: KafkaConfiguration): DataStreamReader =
    stream
      .format("kafka")
      .option("subscribe", config.kafka[String](key = "topics"))
      .option("startingOffsets", config.kafka[String](key = "auto.offset.reset"))
      .option("kafka.bootstrap.servers", config.kafka[String](key = "bootstrap.servers"))

  private[spark] def transformToQueryAndWord(htmlPageDs: Dataset[HtmlPage]) =
    htmlPageDs.flatMap { page =>
      Option(page.body) match {
        case None => Seq.empty
        case Some(body) => parseToWords(body).map(page.query -> _)
      }
    }

  private[spark] def evalQueryScore(spark: SparkSession): DataFrame = spark.sql {
    """  SELECT /*+ BROADCAST(mrk) */
            query,
            COUNT(rv.word) AS score
         FROM CountriesReview rv
         JOIN goodMarkers mrk
            ON rv.word = mrk.word
         GROUP BY query
    """
  }

}


object CountriesReviewSqlStreamJob {

  private lazy val sparkConfig = new SparkConf().setAppName("CountriesReviewSqlStreamJob")
  private lazy val spark = SparkSession.builder().config(sparkConfig).getOrCreate()

  def main(args: Array[String]): Unit = CountriesReviewSqlStreamJob(sparkSession = spark).run(args)

  def apply(sparkSession: SparkSession): CountriesReviewSqlStreamJob = new CountriesReviewSqlStreamJob(sparkSession)

}
