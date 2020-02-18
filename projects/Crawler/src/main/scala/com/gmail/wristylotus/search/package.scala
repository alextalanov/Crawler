package com.gmail.wristylotus

import java.net.URL

import cats.effect.IO

package object search {

  type Query = String

  type Html = List[String]

  type Links = List[URL]

  type Link = URL

  type Header = (String, String)

  case class Content(link: Link, query: Query, body: Html)

  case class ExtractUnit(link: Link, query: Query, content: IO[Html])

}
