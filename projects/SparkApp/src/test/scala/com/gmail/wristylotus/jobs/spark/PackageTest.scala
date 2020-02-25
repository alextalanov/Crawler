package com.gmail.wristylotus.jobs.spark

import com.gmail.wristylotus.jobs.model.HtmlPage
import org.scalatest.{FlatSpec, Matchers}
import specs.withSpark

class PackageTest extends FlatSpec with Matchers {


  "parseToWords" should "return empty list if get empty string" in {
    val html = ""
    val result = parseToWords(html)

    result shouldBe Nil
  }


  it should "parse words in text delimited by [, ! : ; . ? -] and space symbols in <header> and <body> tags" in {
    val html =
      """
        |<!DOCTYPE html>
        |<html>
        |<head>
        |  <meta http-equiv="content-type" content="text/html;charset=utf-8">
        |  <title>Scala Test, for: parse words!method. that checks; something? I - don't know what</title>
        |  <link rel="Stylesheet" href="/assets/stylesheets/main.css" type="text/css" media="screen">
        |</head>
        |<body>
        |  <div>First string</div>
        |  <h1>one,two:three.four!five?six;seven-eight</h1>
        |</body>
        |</html>
        |""".stripMargin

    val expected = Set("Scala", "Test", "for", "parse", "words", "method", "that", "checks", "something", "I",
      "don't", "know", "what", "First", "string", "one", "two", "three", "four", "five", "six", "seven", "eight")
    val result = parseToWords(html).toSet
    val diff = expected &~ result

    diff shouldBe Set.empty
  }


  it should "filter out empty words" in {
    val html =
      """
        |<!DOCTYPE html>
        |<html>
        |<body>
        |  <h1> 1, ,3; ,5,6.    ,8   !  ?  </h1>
        |</body>
        |</html>
        |""".stripMargin

    val expected = Set("1", "3", "5", "6", "8")
    val result = parseToWords(html).toSet

    result shouldEqual expected
  }


  "evalWordsFrequency" should "produce correct result" in {
    val input = List("one", "two", "three", "two", "three", "three")
    val expected = Set("one" -> 1, "two" -> 2, "three" -> 3)
    val result = evalWordsFrequency(input).toSet

    result shouldEqual expected
  }


  "readCsvFiles" should "read csv files" in withSpark { spark =>
    import specs.implicits._
    val expected = Array(HtmlPage(link = "http://aaa.com", query = "where is aaa", body = "content aaa"))

    val csvFile = "/input_csv/data.csv".asResource().toURI
    val result = readCsvFiles(spark, csvFile).collect()

    result shouldEqual expected
  }

}
