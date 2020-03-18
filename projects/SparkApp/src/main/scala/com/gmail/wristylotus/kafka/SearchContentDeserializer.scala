package com.gmail.wristylotus.kafka

import java.nio.charset.StandardCharsets.UTF_8
import java.util

import com.gmail.wristylotus.jobs.model.HtmlPage
import com.google.gson.Gson
import org.apache.kafka.common.serialization.Deserializer

class SearchContentDeserializer extends Deserializer[HtmlPage] with Serializable {

  private lazy val gson = new Gson()

  override def deserialize(topic: String, data: Array[Byte]): HtmlPage = deserialize(new String(data, UTF_8))

  def deserialize(data: String): HtmlPage = gson.fromJson(data, classOf[HtmlPage])

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}
}

object SearchContentDeserializer {
  def apply(): SearchContentDeserializer = new SearchContentDeserializer()
}
