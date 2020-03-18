package com.gmail.wristylotus.jobs.spark

import com.gmail.wristylotus.hbase
import com.gmail.wristylotus.hbase.buildPut
import com.gmail.wristylotus.hbase.model.{Row => HbaseRow, _}
import com.gmail.wristylotus.jobs.configuration
import com.gmail.wristylotus.jobs.configuration.KafkaConfiguration
import com.gmail.wristylotus.jobs.model.HtmlPage
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

class CountriesReviewStreamJob(sparkSession: SparkSession) {

  private implicit val spark = sparkSession

  import configuration.implicits._
  import spark.implicits._

  private[spark] type QueryScore = (String, Long)

  private lazy implicit val hbaseContext = "hbase-config.xml".asResource match {
    case Some(resource) =>
      new HBaseContext(spark.sparkContext, hbase.buildConfiguration(resource))
    case None =>
      throw new IllegalStateException("Can't construct HBase context, config has not been found.")
  }


  def run(args: Array[String]): Unit = {

    val config = KafkaConfiguration(args)

    val checkpoint = config.checkpointDir()
    val durationSec = config.batchDuration.map(Seconds(_))

    val sparkStreaming = StreamingContext.getOrCreate(checkpoint, () => createStreamingContext(durationSec(), checkpoint))

    val stream: InputDStream[ConsumerRecord[String, HtmlPage]] = KafkaUtils.createDirectStream(
      sparkStreaming,
      PreferConsistent,
      Subscribe[String, HtmlPage](config.kafka.topics, config.kafka.asMap)
    )

    val goodReviewWords = spark.sparkContext.broadcast(goodMarkers)

    val htmlPageStream = stream.map(_.value())

    val queryScoreStream = evalQueryScore(htmlPageStream, goodReviewWords.value)

    val aggQueryScoreStream = aggregateByQuery(queryScoreStream)

    aggQueryScoreStream.checkpoint(config.checkpointInterval())

    writeToHbase(aggQueryScoreStream, table = "CountriesReview")

    sparkStreaming.start()
    sparkStreaming.awaitTermination()
  }


  private[spark] def evalQueryScore(pagesStream: DStream[HtmlPage], goodWords: Set[String]): DStream[QueryScore] =
    pagesStream.map { page =>
      Option(page.body) match {
        case None => page.query -> 0
        case Some(body) =>
          val words = parseToWords(body)
          val wordsFrequency = evalWordsFrequency(words)
          val goodWordsFrequency = wordsFrequency.filter {
            case (word, _) => goodWords.contains(word)
          }
          val score = goodWordsFrequency.map(_._2).sum.toLong

          page.query -> score
      }
    }


  private[spark] def aggregateByQuery(queryScoreStream: DStream[QueryScore]): DStream[QueryScore] =
    queryScoreStream.updateStateByKey(
      (values: Seq[Long], state: Option[Long]) => Some(values.sum + state.getOrElse(0L)))


  private[spark] def writeToHbase(stream: DStream[QueryScore], table: String)(implicit context: HBaseContext) = {
    val rowToPut = (row: QueryScore) => buildPut {
      val (query, score) = row

      HbaseRow(
        RowKey(query),
        Column(
          ColumnFamily("A"),
          ColumnQualifier("score"),
          value = score.toString.getBytes
        )
      )
    }

    stream.foreachRDD(rdd => context.bulkPut[QueryScore](rdd, TableName.valueOf(table), rowToPut))
  }

}

object CountriesReviewStreamJob {

  private lazy val sparkConfig = new SparkConf().setAppName("CountriesReviewStreamJob")
  private lazy val spark = SparkSession.builder().config(sparkConfig).getOrCreate()

  def main(args: Array[String]): Unit = CountriesReviewStreamJob(sparkSession = spark).run(args)

  def apply(sparkSession: SparkSession): CountriesReviewStreamJob = new CountriesReviewStreamJob(sparkSession)

}