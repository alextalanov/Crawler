package com.gmail.wristylotus

import java.net.URI
import java.nio.file.Path
import java.util.UUID

import cats.effect.IO
import com.gmail.wristylotus.search.ContentEntry
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path => HPath}
import org.slf4j.LoggerFactory

class CsvFileWriter(
                     hdfs: FileSystem,
                     val filePath: String,
                     val fileSuffix: String = UUID.randomUUID().toString
                   ) extends ContentWriter {

  private val log = LoggerFactory.getLogger(classOf[CsvFileWriter])

  private val writer = hdfs.create(new HPath(filePath).suffix(fileSuffix))

  override def write(entry: ContentEntry): IO[Unit] = IO {
    val ContentEntry(link, query, content) = entry

    content.attempt.unsafeRunSync() match {
      case Right(data) => writer.write(s"""$query,$link,${data.mkString}""".getBytes)
      case Left(ex) => log.warn(ex.getMessage)
    }
  }

  override def close(): Unit = writer.close()

}

object CsvFileWriter {

  def apply(hdfs: FileSystem, filePath: Path): CsvFileWriter = new CsvFileWriter(hdfs, filePath.toString)

  def apply(hdfsUri: URI, filePath: Path): CsvFileWriter = new CsvFileWriter(
    hdfs = FileSystem.get(hdfsUri, new Configuration()),
    filePath = filePath.toString
  )

}
