package com.gmail.wristylotus

import java.nio.file.Path

import cats.effect.{ExitCode, IO, IOApp}
import com.gmail.wristylotus.search.{GoogleSearch, YandexSearch}
import org.rogach.scallop.ScallopConf
import org.slf4j.LoggerFactory

object CrawlerApp extends IOApp {

  val log = LoggerFactory.getLogger(CrawlerApp.getClass)

  // override implicit def contextShift: ContextShift[IO] = ???

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val Google = "google"
    val Yandex = "yandex"

    val engine = opt[String](short = 'e', default = Some(Google)).map(_.toLowerCase)
    val query = opt[String](short = 'q', required = true)
    val dir = opt[Path](short = 'd', required = true)
    verify()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val conf = new Conf(args)

    val contentExtractor = conf.engine.map {
      case conf.Google => new ContentExtractor(conf.query()) with GoogleSearch
      case conf.Yandex => new ContentExtractor(conf.query()) with YandexSearch
    }()

    val extractor = contentExtractor.extract {content =>
      log.info(Thread.currentThread().getName)
      //TODO ContentProcessor
      //TODO ContentWriter
    }

    extractor.unsafeRunSync()

    IO(ExitCode.Success)
  }

}
