package com.gmail.wristylotus.writers

import java.net.URI
import java.nio.file.Path
import java.util.UUID

import cats.effect.IO
import com.gmail.wristylotus.model.AvroContent
import com.gmail.wristylotus.search.Content
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path => HPath}
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName

class ParquetFileWriter(
                         val dfsConfig: Configuration,
                         val filePath: String,
                         val fileSuffix: String = s"_${UUID.randomUUID().toString}"
                       ) extends ContentWriter {

  private val schema = AvroContent.SCHEMA$

  private val file = new HPath(filePath).suffix(fileSuffix)

  private val writer = AvroParquetWriter.builder[AvroContent](file)
    .withSchema(schema)
    .withConf(dfsConfig)
    .withRowGroupSize(ParquetWriter.DEFAULT_BLOCK_SIZE)
    .withPageSize(ParquetWriter.DEFAULT_PAGE_SIZE)
    .withCompressionCodec(CompressionCodecName.SNAPPY)
    .build()


  override def write(content: Content): IO[Unit] = IO {
    writer.write(toAvroContent(content))
  }

  private def toAvroContent(content: Content) =
    AvroContent.newBuilder()
      .setQuery(content.query)
      .setLink(content.link.toString)
      .setBody(content.body.mkString)
      .build()

  override def close(): Unit = writer.close()

}

object ParquetFileWriter {

  def apply(
             dfsConfig: Configuration,
             filePath: Path,
             versioned: Boolean = false
           ): ParquetFileWriter =
    if (versioned) {
      new ParquetFileWriter(dfsConfig, filePath.toString, fileSuffix = "")
    } else {
      new ParquetFileWriter(dfsConfig, filePath.toString)
    }

  def apply(dfsUri: URI, filePath: Path): ParquetFileWriter =
    apply(
      filePath = filePath,
      dfsConfig = new Configuration() {
        set(FileSystem.FS_DEFAULT_NAME_KEY, dfsUri.toString)
      }
    )

}
