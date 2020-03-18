package com.gmail.wristylotus.jobs.configuration

import java.util.Properties

import org.apache.spark.streaming.Seconds
import org.rogach.scallop.ScallopConf

class KafkaConfiguration(arguments: Seq[String]) extends ScallopConf(arguments) {

  private val propertiesFile = opt[String](short = 'p', required = true, default = Some("kafka-config.properties"))

  val batchDuration = opt[Long](short = 'd', required = true)
  val checkpointDir = opt[String](short = 'c', required = true)
  val checkpointInterval = opt[Long](short = 'i', default = Some(5)).map(Seconds(_))

  verify()

  object kafka {

    import implicits._

    import scala.collection.JavaConverters._

    private lazy val properties = propertiesFile.map(_.asResource).map {
      case Some(resource) => new Properties() {
        load(resource.openStream())
      }
      case None =>
        throw new IllegalStateException("Can't find kafka properties file.")
    }()

    lazy val topics = properties.getProperty("topics").split(",").toList

    lazy val asMap: Map[String, Object] =
      properties.entrySet().asScala
        .map(entry => entry.getKey.asInstanceOf[String] -> entry.getValue)
        .toMap

    def apply[T](key: String): T = asMap(key).asInstanceOf[T]

  }

}

object KafkaConfiguration {
  def apply(arguments: Seq[String]): KafkaConfiguration = new KafkaConfiguration(arguments)
}
