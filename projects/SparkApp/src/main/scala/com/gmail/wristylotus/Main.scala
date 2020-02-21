package com.gmail.wristylotus

import com.gmail.wristylotus.jobs.spark.CountriesReviewJob
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Main extends App {

  val conf = new SparkConf().setAppName("CountriesReviewJob")
  lazy val sc = SparkSession.builder().config(conf).getOrCreate()

  CountriesReviewJob(sparkSession = sc).run(args)
}