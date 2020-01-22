package com.gmail.wristylotus

import java.nio.file.{Path, Paths}

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.gmail.wristylotus.search.{ContentEntry, GoogleSearch, YandexSearch}
import org.rogach.scallop.ScallopConf
import org.slf4j.LoggerFactory

import scala.io.Source

object CrawlerApp extends IOApp {

  val log = LoggerFactory.getLogger(CrawlerApp.getClass)

  // override implicit def contextShift: ContextShift[IO] = ???

  val writer: ContentWriter = (_: ContentEntry) => IO.unit


  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val Google = "google"
    val Yandex = "yandex"

    val engine = opt[String](short = 'e', default = Some(Google)).map(_.toLowerCase)
    val query = opt[String](short = 'q', required = true)
    verify()
  }


  override def run(args: List[String]): IO[ExitCode] = {
    val conf = new Conf(args)

    val queryIO = conf.query() match {
      case query if query.endsWith(".txt") => readFile(Paths.get(query))
      case query => IO(List(query))
    }

    val queries = queryIO.unsafeRunSync()

    val contentExtractor = conf.engine.map {
      case conf.Google => new ContentExtractor(queries) with GoogleSearch
      case conf.Yandex => new ContentExtractor(queries) with YandexSearch
    }()

    runExtract(contentExtractor).unsafeRunSync()

    IO(ExitCode.Success)
  }


  def runExtract(extractor: ContentExtractor): IO[Unit] =
    extractor.extract {
      case (link, query, content) =>

        content.map(ContentEntry(link, query, _)).attempt.unsafeRunSync() match {
          case Right(entry) =>
            val w1 = writer(entry)
            val w2 = writer(entry)

            List(w1, w2).parSequence.unsafeRunSync()

          case Left(ex) => log.warn(ex.getMessage)
        }
    }


  def readFile(path: Path): IO[List[String]] = {
    val io = IO(Source.fromFile(path.toUri))
    val resource = Resource.make(io)(in => IO(in.close()))
    resource.use(in => IO(in.getLines().toList))
  }

}
