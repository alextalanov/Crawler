package com.gmail.wristylotus.writers

import java.net.URI
import java.nio.file.Path
import java.util.UUID

import cats.effect.IO
import com.gmail.wristylotus.search.Content
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path => HPath}

class CsvFileWriter(
                     hdfs: FileSystem,
                     val filePath: String,
                     val fileSuffix: String = s"_${UUID.randomUUID().toString}"
                   ) extends ContentWriter {

  private val writer = hdfs.create(new HPath(filePath).suffix(fileSuffix))

  override def write(content: Content): IO[Unit] = IO {
    val Content(link, query, body) = content
    val row = s"""$query,$link,${body.mkString}""".getBytes

    writer.write(row)
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
