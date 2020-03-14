package com.gmail.wristylotus.kafka

import java.util

import com.gmail.wristylotus.jobs.model.HtmlPage
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import org.apache.kafka.common.serialization.Deserializer

class SearchContentDeserializer extends Deserializer[HtmlPage] {

  private val gson = new Gson()

  override def deserialize(topic: String, data: Array[Byte]): HtmlPage =
    gson.fromJson(
      new String(data, StandardCharsets.UTF_8), classOf[HtmlPage])

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}
}
