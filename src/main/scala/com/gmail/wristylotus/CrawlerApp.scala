package com.gmail.wristylotus

import java.net.URI
import java.nio.file.{Path, Paths}

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.gmail.wristylotus.search.{GoogleSearch, YandexSearch}
import org.rogach.scallop.ScallopConf
import org.slf4j.LoggerFactory

import scala.io.Source

object CrawlerApp extends IOApp {

  val log = LoggerFactory.getLogger(CrawlerApp.getClass)

  // override implicit def contextShift: ContextShift[IO] = ???

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val Google = "google"
    val Yandex = "yandex"

    val engine = opt[String](short = 'e', default = Some(Google)).map(_.toLowerCase)
    val query = opt[String](short = 'q', required = true)
    val hdfsAddr = opt[URI](short = 'a', required = true)
    val filePath = opt[Path](short = 'f', required = true)
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

    def writer = CsvFileWriter(conf.hdfsAddr(), conf.filePath())

    contentExtractor.extractWith(writer).unsafeRunSync()

    IO(ExitCode.Success)
  }

  def readFile(path: Path): IO[List[String]] = {
    val io = IO(Source.fromFile(path.toUri))
    val resource = Resource.make(io)(in => IO(in.close()))
    resource.use(in => IO(in.getLines().toList))
  }

}
