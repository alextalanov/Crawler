package com.gmail.wristylotus.writers

import java.net.URI
import java.nio.file.Path
import java.util.UUID

import cats.effect.IO
import com.gmail.wristylotus.search.Content
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path => HPath}

class CsvFileWriter(
                     dfs: FileSystem,
                     val filePath: String,
                     val fileSuffix: String = s"_${UUID.randomUUID().toString}"
                   ) extends ContentWriter {

  private val writer = dfs.create(new HPath(filePath).suffix(fileSuffix))

  override def write(content: Content): IO[Unit] = IO {
    val Content(link, query, html) = content

    val noCommasHtml = html.mkString.replaceAll(",", "")

    val row = s"""$link,$query,$noCommasHtml\n""".getBytes

    writer.write(row)
  }

  override def close(): Unit = writer.close()

}

object CsvFileWriter {

  def apply(
             dfsConfig: Configuration,
             filePath: Path,
             versioned: Boolean = false
           ): CsvFileWriter =
    if (versioned) {
      new CsvFileWriter(FileSystem.get(dfsConfig), filePath.toString, fileSuffix = "")
    } else {
      new CsvFileWriter(FileSystem.get(dfsConfig), filePath.toString)
    }


  def apply(dfsUri: URI, filePath: Path): CsvFileWriter =
    apply(
      filePath = filePath,
      dfsConfig = new Configuration() {
        set(FileSystem.FS_DEFAULT_NAME_KEY, dfsUri.toString)
      }
    )
}
