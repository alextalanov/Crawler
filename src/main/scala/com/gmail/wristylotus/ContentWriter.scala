package com.gmail.wristylotus

import cats.effect.IO
import com.gmail.wristylotus.search.ContentEntry

trait ContentWriter {

  def write(entry: ContentEntry): IO[Unit]

  def apply(entry: ContentEntry): IO[Unit] = write(entry)

}
