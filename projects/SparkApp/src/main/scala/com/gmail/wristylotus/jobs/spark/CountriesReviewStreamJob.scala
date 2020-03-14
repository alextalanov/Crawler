package com.gmail.wristylotus.jobs.spark

import com.gmail.wristylotus.jobs.configuration.KafkaConfiguration
import com.gmail.wristylotus.jobs.model.HtmlPage
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils

class CountriesReviewStreamJob(sparkSession: SparkSession) {

  private val spark = sparkSession

  import spark.implicits._

  def run(args: Array[String]): Unit = {
    val config = KafkaConfiguration(args)

    val sparkStreaming = new StreamingContext(spark.sparkContext, config.batchDuration())

    val stream = KafkaUtils.createDirectStream(
      sparkStreaming,
      PreferConsistent,
      Subscribe[String, HtmlPage](config.kafka.topics, config.kafka.asMap)
    )

    stream.map(_.value().query).print()

    //TODO implement logic

    sparkStreaming.start()
    sparkStreaming.awaitTermination()
  }

}

object CountriesReviewStreamJob {

  private lazy val sparkConfig = new SparkConf().setAppName("CountriesReviewStreamJob")
  private lazy val spark = SparkSession.builder().config(sparkConfig).getOrCreate()

  def main(args: Array[String]): Unit = CountriesReviewStreamJob(sparkSession = spark).run(args)

  def apply(sparkSession: SparkSession): CountriesReviewStreamJob = new CountriesReviewStreamJob(sparkSession)

}