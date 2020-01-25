package com.gmail.wristylotus.search

import java.net.URL

import cats.effect.IO

case class ContentEntry(link: URL, query: String, content: IO[List[String]])
