package com.gmail.wristylotus.writers.kafka

import com.gmail.wristylotus.search.Content
import com.google.gson.Gson
import org.apache.commons.io.Charsets
import org.apache.kafka.common.serialization.Serializer

class SearchContentSerializer extends Serializer[Content] {

  private val gson = new Gson()

  override def serialize(topic: String, data: Content): Array[Byte] = {
    val Content(link, query, body) = data

    val message = gson.toJson(
      Map(
        "link" -> link,
        "query" -> query,
        "body" -> body.mkString
      )
    )

    message.getBytes(Charsets.UTF_8)
  }

}
