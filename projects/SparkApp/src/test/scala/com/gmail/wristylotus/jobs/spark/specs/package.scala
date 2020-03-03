package com.gmail.wristylotus.jobs.spark

import org.apache.spark.sql.{SQLContext, SparkSession}
import org.scalatest.Assertion

package object specs {
  /*
    //Discard of loading system properties
    private[this] lazy val sc = {
      val conf = new SparkConf(false).setMaster("local").setAppName("SparkJobTest")
      SparkContext.getOrCreate(conf)
    }

    lazy val sparkSession = new SQLContext(sc).sparkSession
  */

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
