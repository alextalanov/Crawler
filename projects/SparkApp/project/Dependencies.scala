import sbt._

object Dependencies {

  lazy val sparkVersion = "2.4.0"

  lazy val kafkaVersion = "2.4.0"

  lazy val logbackVersion = "1.2.3"

  val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion % Provided

  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided

  val sparkSqlKafkaStreaming = "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % Provided

  val sparkKafkaStreaming = "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % Provided

  val sparkHBaseConnector = ("org.apache.hbase.connectors.spark" % "hbase-spark" % "1.0.0" % Compile)
    .excludeAll(
      ExclusionRule(organization = "org.apache.spark"),
      ExclusionRule(organization = "com.fasterxml.jackson.core"),
      ExclusionRule(organization = "com.sun.jersey"),
      ExclusionRule(organization = "javax.servlet"),
      ExclusionRule(organization = "javax.servlet.jsp"),
      ExclusionRule(organization = "javax.inject"),
      ExclusionRule(organization = "aopalliance")
    )

  val kafkaStreams = ("org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion % Compile)
    .excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core"))

  val jsoup = "org.jsoup" % "jsoup" % "1.12.1" % Compile

  val scallop = "org.rogach" %% "scallop" % "3.4.0" % Compile

  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion % Compile

  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVersion % Compile

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0" % Compile

  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.1" % Test

  val scalamock = "org.scalamock" %% "scalamock" % "4.4.0" % Test

  val spark = Seq(sparkSql, sparkStreaming, sparkHBaseConnector, sparkSqlKafkaStreaming, sparkKafkaStreaming)

  val common = Seq(jsoup, scallop, kafkaStreams, logbackCore, logbackClassic, scalaLogging)

  val testing = Seq(scalatest, scalacheck, scalamock)

}
