package com.gmail.wristylotus

import java.net.URL

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.gmail.wristylotus.search.SearchEngine

class ContentExtractor(
                        queries: List[String],
                        concurrency: Int = Runtime.getRuntime.availableProcessors()
                      )(
                        implicit contextShift: ContextShift[IO]
                      ) {
  searchEngine: SearchEngine =>

  type Consumer = ((URL, String, IO[Content])) => Unit

  def extract(consumer: Consumer): IO[Unit] =
    queries.map { query =>
      search(query)
        .map(links => links.grouped(concurrency))
        .map(chunks => chunks.map(extractContent(_, query, consumer)).toList.parSequence)
        .flatten
    }
      .sequence_


  private def extractContent(links: Links, query: String, consumer: Consumer): IO[Unit] = IO {
    links.view.map(link => (link, query, readContent(link))).foreach(consumer)
  }

}
