package com.gmail.wristylotus.search

import java.net.{URL, URLConnection, URLEncoder}
import java.nio.charset.StandardCharsets.UTF_8

import cats.effect.{IO, Resource}

import scala.io.{BufferedSource, Source}

trait SearchEngine {

  type Query = String

  type Content = Iterator[String]

  type Links = Set[URL]

  type Header = (String, String)

  val url: String

  def search(query: Query): IO[Links] = readContent(buildUrl(query)).map(parse)

  protected def parse(content: Content): Links

  protected def buildUrl(query: Query): URL = new URL(url + encode(query))

  protected def encode(query: Query): Query = URLEncoder.encode(query, UTF_8.toString).toLowerCase

  protected def withHeaders(connection: URLConnection, headers: Header*): URLConnection = {
    headers.foreach {
      case (name, value) => connection.setRequestProperty(name, value)
    }
    connection
  }

  protected def openConnection(url: URL): Resource[IO, BufferedSource] = {
    val io = IO {
      val connection = withHeaders(url.openConnection(), "User-Agent" -> "Mozilla/5.0")
      Source.fromInputStream(connection.getInputStream)
    }
    Resource.make(io)(in => IO(in.close()))
  }

  protected def readContent(url: URL): IO[Content] = openConnection(url).use(in => IO(in.getLines()))

}
