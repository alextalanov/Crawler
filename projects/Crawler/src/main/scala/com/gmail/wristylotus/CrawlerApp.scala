package com.gmail.wristylotus

import java.net.URI
import java.nio.file.{Path, Paths}

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.gmail.wristylotus.search.{GoogleSearch, YandexSearch}
import com.gmail.wristylotus.writers.{CsvFileWriter, ParquetFileWriter}
import org.rogach.scallop.ScallopConf
import org.slf4j.LoggerFactory

import scala.io.Source

object CrawlerApp extends IOApp {

  val log = LoggerFactory.getLogger(CrawlerApp.getClass)

  // override implicit def contextShift: ContextShift[IO] = ???

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    object Engine {
      val Google = "google"
      val Yandex = "yandex"
    }

    object ExtractFormat {
      val Csv = "csv"
      val Parquet = "parquet"
    }

    val engine = opt[String](short = 'e', default = Some(Engine.Google)).map(_.toLowerCase)
    val format = opt[String](short = 'm', default = Some(ExtractFormat.Csv).map(_.toLowerCase))
    val query = opt[String](short = 'q', required = true)
    val hdfsAddr = opt[URI](short = 'a', required = true)
    val filePath = opt[Path](short = 'f', required = true)
    verify()
  }

  //sbt runMain com.gmail.wristylotus.CrawlerApp -q queries.txt -a "hdfs://namenode:9000" -f com/gmail/wristylotus/crawler/data.(csv, parquet)
  override def run(args: List[String]): IO[ExitCode] = {
    val conf = new Conf(args)

    val queryIO = conf.query() match {
      case query if query.endsWith(".txt") => readFile(Paths.get(query))
      case query => IO(List(query))
    }

    val queries = queryIO.unsafeRunSync()

    val contentExtractor = conf.engine.map {
      case conf.Engine.Google => new ContentExtractor(queries) with GoogleSearch
      case conf.Engine.Yandex => new ContentExtractor(queries) with YandexSearch
    }()

    def writer = conf.format.map {
      case conf.ExtractFormat.Csv => CsvFileWriter(conf.hdfsAddr(), conf.filePath())
      case conf.ExtractFormat.Parquet => ParquetFileWriter(conf.hdfsAddr(), conf.filePath())
    }()

    contentExtractor.extractWith(writer).unsafeRunSync()

    IO(ExitCode.Success)
  }

  def readFile(path: Path): IO[List[String]] = {
    val io = IO(Source.fromFile(path.toUri))
    val resource = Resource.make(io)(in => IO(in.close()))
    resource.use(in => IO(in.getLines().toList))
  }

}
