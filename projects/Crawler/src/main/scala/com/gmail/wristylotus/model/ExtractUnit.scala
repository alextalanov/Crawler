package com.gmail.wristylotus.model

import java.net.URL

import cats.effect.IO

case class ExtractUnit(link: URL, query: String, content: IO[List[String]])
