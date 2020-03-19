package com.gmail.wristylotus.jobs.spark


import com.gmail.wristylotus.jobs.model.HtmlPage
import org.apache.hadoop.hbase.TableName
import com.gmail.wristylotus.jobs.configuration
import com.gmail.wristylotus.jobs.configuration.SparkJobConfiguration
import com.gmail.wristylotus.hbase
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.gmail.wristylotus.hbase.model.{Row => HbaseRow, _}
import com.gmail.wristylotus.hbase.buildPut
import org.apache.spark.SparkConf

class CountriesReviewJob(sparkSession: SparkSession) {

  private val spark = sparkSession

  import spark.implicits._
  import configuration.implicits._

  private lazy implicit val hbaseContext = "hbase-config.xml".asResource match {
    case Some(resource) =>
      new HBaseContext(spark.sparkContext, hbase.buildConfiguration(resource))
    case None =>
      throw new IllegalStateException("Can't construct HBase context, config has not been found.")
  }


  def run(args: Array[String]): Unit = {

    val conf = SparkJobConfiguration(args)

    val goodReviewWords = spark.sparkContext.broadcast(goodMarkers)

    val htmlPagesDS = readParquetFiles(spark, conf.hdfs.input())

    val queryScoreDS = evalQueryScore(htmlPagesDS, goodReviewWords.value)

    val aggQueryScoreDS = aggregateByQuery(queryScoreDS)

    writeToHbase(aggQueryScoreDS.rdd, TableName.valueOf("CountriesReview"))
  }


  private[spark] def evalQueryScore(pagesDS: Dataset[HtmlPage], goodWords: Set[String]) =
    pagesDS.map { page =>
      Option(page.body) match {
        case None => page.query -> 0
        case Some(body) =>
          val words = parseToWords(body)
          val wordsFrequency = evalWordsFrequency(words)
          val goodWordsFrequency = wordsFrequency.filter {
            case (word, _) => goodWords.contains(word)
          }
          val score = goodWordsFrequency.map(_._2).sum

          page.query -> score
      }
    }


  private[spark] def aggregateByQuery(queryScoreDS: Dataset[(String, Int)]) =
    queryScoreDS
      .toDF("query", "score")
      .groupBy($"query")
      .agg(sum($"score").alias("score"))


  private[spark] def writeToHbase(rdd: RDD[Row], table: TableName)(implicit context: HBaseContext) = {
    val rowToPut = (row: Row) => buildPut {
      HbaseRow(
        RowKey(row.getAs[String]("query")),
        Column(
          ColumnFamily("A"),
          ColumnQualifier("score"),
          value = row.getAs[Long]("score").toString.getBytes
        )
      )
    }

    context.bulkPut[Row](rdd = rdd, tableName = table, rowToPut)
  }

}


object CountriesReviewJob {

  private lazy val sparkConfig = new SparkConf().setAppName("CountriesReviewJob")
  private lazy val spark = SparkSession.builder().config(sparkConfig).getOrCreate()

  def main(args: Array[String]): Unit = CountriesReviewJob(sparkSession = spark).run(args)

  def apply(sparkSession: SparkSession): CountriesReviewJob = new CountriesReviewJob(sparkSession)

}