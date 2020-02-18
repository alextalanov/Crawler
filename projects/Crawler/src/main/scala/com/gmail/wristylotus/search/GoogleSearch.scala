package com.gmail.wristylotus.search

import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.util.matching.Regex

trait GoogleSearch extends SearchEngine {

  override val url: String = "https://www.google.com/search?q="

  private val linkExtract: Regex = "^/url\\?q=(.*)&sa=".r

  override def parse(content: Html): Links = {
    val doc = Jsoup.parse(content.mkString)
    val hrefs = doc.body().getAllElements.eachAttr("href").asScala

    hrefs
      .flatMap(extractLink)
      .filterNot(_.contains("google"))
      .map(new Link(_))
      .toList
  }

  private def extractLink(link: String) =
    linkExtract.findAllMatchIn(link).map(_.group(1))

}
