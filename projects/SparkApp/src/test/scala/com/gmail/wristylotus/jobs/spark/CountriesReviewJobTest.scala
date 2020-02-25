package com.gmail.wristylotus.jobs.spark

import com.gmail.wristylotus.jobs.model.HtmlPage
import com.gmail.wristylotus.jobs.spark.specs.SparkJobFixtureUnitSpec


class CountriesReviewJobTest extends SparkJobFixtureUnitSpec {

  override type JobType = CountriesReviewJob

  override def withJob = CountriesReviewJob(sparkSession = spark)

  import spark.implicits._


  "aggregateByQuery" should "return empty array when passed Dataset is empty" in { job =>
    val emptyDS = spark.emptyDataset[(String, Int)]
    val result = job.aggregateByQuery(emptyDS).collect()

    result shouldBe Array.empty
  }


  it should "produce correct results" in { job =>
    val table = Table(
      ("expected", "dataset"),
      (Set("A" -> 3L, "B" -> 1L), List("A" -> 2, "B" -> 1, "A" -> 1).toDS()),
      (Set("B" -> 2L, "A" -> 5L), List("B" -> 1, "B" -> 1, "A" -> 5).toDS())
    )

    forAll(table) { (expected, dataset) =>
      val result = job.aggregateByQuery(dataset)
        .map(row => row.getString(0) -> row.getLong(1)).collect().toSet

      result shouldEqual expected
    }
  }


  "evalQueryScore" should "produce zero result for empty HtmlPage's body" in { job =>
    val Query = "query"
    val table = Table(
      "pageDS",
      List(HtmlPage("", Query, body = "")).toDS(),
      List(HtmlPage("", Query, body = null)).toDS()
    )
    val expected = Array(Query -> 0)

    forAll(table) { page =>
      val result = job.evalQueryScore(page, Set.empty).collect()

      result shouldEqual expected
    }
  }


  it should "correct filter good words" in { job =>
    val Query = "query"
    val table = Table(
      ("expected", "pageDS", "goodWords"),
      (Array(Query -> 4), List(HtmlPage("", Query, body = "<html><body>a, r, b, a, , c, d ! e</body></html>")).toDS(), Set("a", "b", "c")),
      (Array(Query -> 2), List(HtmlPage("", Query, body = "<html><body>a, r, b,, d ! e f k; n</body></html>")).toDS(), Set("a", "e", "c"))
    )

    forAll(table) { (expected, page, words) =>
      val result = job.evalQueryScore(page, words).collect()

      result shouldEqual expected
    }
  }

}
