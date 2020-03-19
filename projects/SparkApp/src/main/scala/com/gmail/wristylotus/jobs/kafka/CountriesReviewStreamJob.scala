package com.gmail.wristylotus.jobs.kafka

import com.gmail.wristylotus.jobs.configuration.KafkaStreamingConfiguration
import com.gmail.wristylotus.jobs.model.HtmlPage
import com.gmail.wristylotus.kafka.SearchContentDeserializer
import com.typesafe.scalalogging.Logger
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.Serdes
import org.apache.kafka.streams.scala.ImplicitConversions
import org.apache.kafka.streams.scala.kstream.KStream
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import scala.sys.ShutdownHookThread

class CountriesReviewStreamJob {

  import Serdes._
  import ImplicitConversions._
  import scala.concurrent.duration._

  private val Log = Logger(LoggerFactory.getLogger(getClass))

  private[kafka] val goodMarkers = Set("admirable", "adorable", "alluring", "angelic", "appealing", "beauteous", "bewitching", "captivating", "charming", "classy", "comely", "cute", "dazzling", "delicate", "delightful", "divine", "elegant", "enthralling", "enticing", "excellent", "exquisite", "fair", "fascinating", "fetching", "fine", "foxy", "good-looking", "gorgeous", "graceful", "grand", "handsome", "ideal", "inviting", "lovely", "magnetic", "magnificent", "marvelous", "mesmeric", "nice", "pleasing", "pretty", "pulchritudinous", "radiant", "ravishing", "refined", "resplendent", "shapely", "slightly", "splendid", "statuesque", "stunning", "sublime", "superb", "symmetrical", "taking", "tantalizing", "teasing", "tempting", "well-formed", "winning", "wonderful")

  def run(args: Seq[String]): Unit = {

    val config = KafkaStreamingConfiguration(args)

    val deserializer = SearchContentDeserializer()

    val builder = new StreamsBuilder()

    val pageStream = builder.stream[String, String](config.kafka.topic).mapValues(deserializer.deserialize(_))

    val queryScoreStream = evalQueryScore(pageStream)

    val queryScoreTable = queryScoreStream
      .groupByKey
      .reduce(_ + _)(Materialized.as("query-score-store"))

    queryScoreTable.toStream.to(config.kafka.outputTopic)

    val kafka = new KafkaStreams(builder.build(), config.kafka.props)

    registerStopKafkaShutdownHook(kafka, timeout = 1.minute)

    kafka.start()
  }

  private[kafka] def evalQueryScore(pageStream: KStream[String, HtmlPage]): KStream[String, Int] =
    pageStream.map { (_, page) =>
      Option(page.body) match {
        case None => page.query -> 0
        case Some(body) =>
          val words = parseToWords(body)
          val goodWords = words.filter(goodMarkers.contains)
          val score = goodWords.length

          Log.info(s"""${page.query}, $score""")

          page.query -> score
      }
    }


  private[kafka] def parseToWords(html: String): List[String] = {
    val sepPattern = "[,!:;.?\\s\\-]"
    val doc = Jsoup.parse(html)
    val words = doc.body().text().split(sepPattern) ++ doc.head().text().split(sepPattern)

    words.filter(_.nonEmpty).toList
  }

  private def registerStopKafkaShutdownHook(kafka: KafkaStreams, timeout: Duration): ShutdownHookThread = {
    sys.ShutdownHookThread {
      val isClosed = kafka.close(java.time.Duration.ofMillis(timeout.toMillis))
      if (isClosed) {
        Log.info("All threads have been stopped successfully!")
      } else {
        Log.warn("Not all threads have been stopped!")
      }
    }
  }

}

object CountriesReviewStreamJob {

  def main(args: Array[String]): Unit = CountriesReviewStreamJob().run(args)

  def apply(): CountriesReviewStreamJob = new CountriesReviewStreamJob()

}
