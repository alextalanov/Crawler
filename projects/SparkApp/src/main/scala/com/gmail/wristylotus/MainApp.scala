package com.gmail.wristylotus

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object MainApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SparkExample")
    val sc = SparkSession.builder().config(conf).getOrCreate()

    def genValues(start: Int = 0): Stream[Int] = start #:: genValues(start + 1)

    val data = genValues().take(10000000).force
    println(s"Parallelize data: ${data.take(1000).force.toList}")

    sc.sparkContext.parallelize(data)
      .map(v => (v, v))
      .reduceByKey(_ + _)
      .foreachPartition(_.foreach {
        case (key, value) => println(s"$key, $value")
      })

    sc.stop()
  }

}
