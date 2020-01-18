package com.gmail.wristylotus

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.gmail.wristylotus.search.SearchEngine

class ContentExtractor(
                        query: String,
                        concurrency: Int = Runtime.getRuntime.availableProcessors()
                      )(
                        implicit contextShift: ContextShift[IO]
                      ) {
  searchEngine: SearchEngine =>

  type Consumer = IO[Content] => Unit

  def extract(consumer: Consumer): IO[Unit] =
    search(query)
      .map(links => links.grouped(concurrency))
      .map(chunks => chunks.map(extractContent(_, consumer)).toList.parSequence)
      .flatten
      .void

  private def extractContent(links: Links, consumer: Consumer): IO[Unit] = IO {
    links.view.map(readContent).foreach(consumer)
  }

}
