package com.gmail.wristylotus.jobs.spark


import com.gmail.wristylotus.jobs.model.HtmlPage
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

class CountriesReviewJob(sparkSession: SparkSession) extends Serializable {

  lazy val spark = sparkSession

  import spark.implicits._

  def run(args: Seq[String]): Unit = {
    val conf = config(args)

    val goodReviewWords = spark.sparkContext.broadcast(goodMarkers)

    val htmlPagesDS = readCsvFiles(spark, conf.hdfs.input())

    val queryScoreDS = htmlPagesDS.map(page => evalQueryScore(page, goodReviewWords.value))

    val groupedQueryScoreDS = queryScoreDS
      .toDF("query", "score")
      .groupBy($"query")
      .agg(sum($"score").alias("score"))

    groupedQueryScoreDS.coalesce(1).write.csv(conf.hdfs.output().getPath)
  }

  def evalQueryScore(page: HtmlPage, goodWords: Set[String]) =
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

object CountriesReviewJob {
  def apply(sparkSession: SparkSession): CountriesReviewJob = new CountriesReviewJob(sparkSession)
}
