package com.gmail.wristylotus.jobs.configuration

import java.util.Properties

import org.rogach.scallop.ScallopConf

class KafkaStreamingConfiguration(arguments: Seq[String]) extends ScallopConf(arguments) {

  private val propertiesFile = opt[String](short = 'p', required = true, default = Some("kafka-config.properties"))

  verify()

  object kafka {

    import implicits._

    lazy val props = propertiesFile.map(_.asResource).map {
      case Some(resource) => new Properties() {
        load(resource.openStream())
      }
      case None =>
        throw new IllegalStateException("Can't find kafka properties file.")
    }()

    lazy val topic = props.getProperty("topics").split(",")(0)

    lazy val outputTopic = props.getProperty("outputTopic")

  }

}

object KafkaStreamingConfiguration {
  def apply(arguments: Seq[String]): KafkaStreamingConfiguration = new KafkaStreamingConfiguration(arguments)
}
