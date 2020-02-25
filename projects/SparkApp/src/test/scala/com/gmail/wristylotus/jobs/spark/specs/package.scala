package com.gmail.wristylotus.jobs.spark

import org.apache.spark.sql.SparkSession
import org.scalatest.Assertion

package object specs {

  lazy val sparkSession = SparkSession
    .builder()
    .master("local")
    .appName("SparkJobTest")
    .getOrCreate()

  def withSpark(test: SparkSession => Assertion): Assertion = test(sparkSession)

  object implicits {

    implicit class ResourceOps(path: String) {
      def asResource() = getClass.getResource(path)
    }

  }

}
