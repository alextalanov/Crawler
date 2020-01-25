package com.gmail.wristylotus.search

import java.net.URL

import org.jsoup.Jsoup

import collection.JavaConverters._
import scala.util.matching.Regex

trait GoogleSearch extends SearchEngine {

  override val url: String = "https://www.google.com/search?q="

  private val linkExtract: Regex = "^/url\\?q=(.*)&sa=".r

  override def parse(content: Content): Links = {
    val doc = Jsoup.parse(content.mkString)
    val hrefs = doc.body().getAllElements.eachAttr("href").asScala

    hrefs
      .flatMap(extractLink)
      .filterNot(_.contains("google"))
      .map(new URL(_))
      .toList
  }

  private def extractLink(link: String) =
    linkExtract.findAllMatchIn(link).map(_.group(1))

}
