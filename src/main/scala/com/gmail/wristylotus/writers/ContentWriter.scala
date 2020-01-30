package com.gmail.wristylotus.writers

import cats.effect.IO
import com.gmail.wristylotus.model
import com.gmail.wristylotus.model.{Content, ExtractUnit}
import org.slf4j.LoggerFactory

trait ContentWriter extends AutoCloseable {

  private val log = LoggerFactory.getLogger(classOf[ContentWriter])

  def write(content: Content): IO[Unit]

  def apply(entry: ExtractUnit): IO[Unit] = {

    val ExtractUnit(link, query, content) = entry

    content.attempt.unsafeRunSync() match {
      case Right(data) => write(model.Content(link, query, data))
      case Left(ex) => IO(log.warn(ex.getMessage))
    }
  }

}
