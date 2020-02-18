package com.gmail.wristylotus

import cats.Functor
import cats.effect.{ContextShift, IO, Resource}
import cats.implicits._
import com.gmail.wristylotus.search.{ExtractUnit, Link, Query, SearchEngine}
import com.gmail.wristylotus.writers.ContentWriter

class ContentExtractor(
                        queries: List[Query],
                        concurrency: Int = Runtime.getRuntime.availableProcessors()
                      )(
                        implicit contextShift: ContextShift[IO]
                      ) {
  searchEngine: SearchEngine =>

  def extractWith(writer: => ContentWriter): IO[Unit] = queries
    .map(query => searchWith(query))
    .parSequence
    .map(_.flatten)
    .map(splitToPartitions)
    .flatMap(divideBtwWorkers(_, writer).parSequence)
    .void

  private def searchWith(query: Query) =
    (Functor[IO] compose Functor[List]).map(search(query))((query, _))

  protected def splitToPartitions(links: List[(Query, Link)]) =
    links.grouped(links.size / concurrency).toList

  protected def divideBtwWorkers(partitions: List[List[(Query, Link)]], writer: => ContentWriter) =
    partitions.map(extractContent(_, writer))

  protected def extractContent(links: List[(Query, Link)], contentWriter: ContentWriter): IO[Unit] =
    Resource.fromAutoCloseable(IO(contentWriter)).use { writer =>
      IO {
        links.view
          .map { case (query, link) => ExtractUnit(link, query, readContent(link)) }
          .map(writer(_))
          .foreach(_.unsafeRunSync())
      }
    }

}
