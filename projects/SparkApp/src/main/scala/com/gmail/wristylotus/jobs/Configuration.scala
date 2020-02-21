package com.gmail.wristylotus.jobs

import java.net.URI

import org.rogach.scallop.ScallopConf

class Configuration(arguments: Seq[String]) extends ScallopConf(arguments) {
  val hdfsAddr = opt[URI](short = 'a', required = true)
  private val in = opt[String](short = 'i', required = true)
  private val out = opt[String](short = 'o', required = true)

  verify()

  object hdfs {
    lazy val input = in.map(hdfsAddr().resolve)
    lazy val output = out.map(hdfsAddr().resolve)
  }

}

object Configuration {
  def apply(arguments: Seq[String]): Configuration = new Configuration(arguments)
}
