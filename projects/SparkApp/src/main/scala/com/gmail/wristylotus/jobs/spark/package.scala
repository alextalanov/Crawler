package com.gmail.wristylotus.jobs

import java.net.URI

import com.gmail.wristylotus.jobs.model.HtmlPage
import org.apache.spark.sql.{Dataset, SparkSession}
import org.jsoup.Jsoup

package object spark {

  val inputColumns = List("link", "query", "body")
  val goodMarkers = Set("admirable", "adorable", "alluring", "angelic", "appealing", "beauteous", "bewitching", "captivating", "charming", "classy", "comely", "cute", "dazzling", "delicate", "delightful", "divine", "elegant", "enthralling", "enticing", "excellent", "exquisite", "fair", "fascinating", "fetching", "fine", "foxy", "good-looking", "gorgeous", "graceful", "grand", "handsome", "ideal", "inviting", "lovely", "magnetic", "magnificent", "marvelous", "mesmeric", "nice", "pleasing", "pretty", "pulchritudinous", "radiant", "ravishing", "refined", "resplendent", "shapely", "slightly", "splendid", "statuesque", "stunning", "sublime", "superb", "symmetrical", "taking", "tantalizing", "teasing", "tempting", "well-formed", "winning", "wonderful")

  def config(args: Seq[String]) = Configuration(args)

  def readCsvFiles(spark: SparkSession, input: URI): Dataset[HtmlPage] = {
    import spark.implicits._
    spark.read
      .option("quote", "")
      .csv(input.getPath)
      .toDF(inputColumns: _*)
      .as[HtmlPage]
  }

  def parseToWords(html: String): List[String] = {
    val sepPattern = "[,!:;.?\\s\\-]"
    val doc = Jsoup.parse(html)
    val words = doc.body().text().split(sepPattern) ++ doc.head().text().split(sepPattern)

    words.filter(_.nonEmpty).toList
  }

  def evalWordsFrequency(words: List[String]): List[(String, Int)] =
    words.groupBy(identity).mapValues(_.length).toList

}
