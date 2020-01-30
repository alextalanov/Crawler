package com.gmail.wristylotus

import java.net.URL

import cats.Functor
import cats.effect.{ContextShift, IO, Resource}
import cats.implicits._
import com.gmail.wristylotus.model.ExtractUnit
import com.gmail.wristylotus.search.SearchEngine
import com.gmail.wristylotus.writers.ContentWriter

class ContentExtractor(
                        queries: List[String],
                        concurrency: Int = Runtime.getRuntime.availableProcessors()
                      )(
                        implicit contextShift: ContextShift[IO]
                      ) {
  searchEngine: SearchEngine =>

  def extractWith(writer: => ContentWriter): IO[Unit] = queries
    .map { query => (Functor[IO] compose Functor[List]).map(search(query))((query, _)) }
    .parSequence
    .map(_.flatten)
    .map(links => links.grouped(links.size / concurrency).toList)
    .map(_.map(extractContent(_, writer)).parSequence)
    .flatten
    .void


  private def extractContent(links: List[(Query, URL)], contentWriter: ContentWriter): IO[Unit] =
    Resource.fromAutoCloseable(IO(contentWriter)).use { writer =>
      IO {
        links.view
          .map { case (query, link) => ExtractUnit(link, query, readContent(link)) }
          .map(writer(_))
          .foreach(_.unsafeRunSync())
      }
    }

}
