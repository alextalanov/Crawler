package com.gmail.wristylotus.writers.kafka

import java.util.Properties

import cats.effect.IO
import com.gmail.wristylotus.search.Content
import com.gmail.wristylotus.writers.ContentWriter
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConverters._

class KafkaWriter(topic: String, config: Map[String, AnyRef]) extends ContentWriter {

  private val kafka = new KafkaProducer[String, Content](config.asJava)

  override def write(content: Content): IO[Unit] = IO {
    kafka.send(new ProducerRecord[String, Content](topic, content))
  }

  override def close(): Unit = kafka.close()

}

object KafkaWriter {

  def apply(topic: String, config: Map[String, AnyRef]): KafkaWriter = new KafkaWriter(topic, config)

  def apply(config: Properties): KafkaWriter = {
    val convertedConfig = config.entrySet().asScala
      .map(entry => entry.getKey.asInstanceOf[String] -> entry.getValue)
      .toMap

    new KafkaWriter(
      topic = config.getProperty("topic"),
      config = convertedConfig
    )
  }

}
