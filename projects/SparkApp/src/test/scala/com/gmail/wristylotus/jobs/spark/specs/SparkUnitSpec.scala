package com.gmail.wristylotus.jobs.spark.specs

import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, fixture}

trait SparkUnitSpec extends fixture.FlatSpec with Matchers with PropertyChecks {

  val spark = sparkSession

  spark.sparkContext.setLogLevel("ERROR")

}
