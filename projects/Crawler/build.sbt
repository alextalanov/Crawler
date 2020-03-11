name := "Crawler"
version := "0.1"
scalaVersion := "2.12.10"

conflictManager := ConflictManager.strict

lazy val hadoopVersion = "2.9.0"

libraryDependencies ++= Seq(
  // Common
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.julien-truffaut" %% "monocle-core" % "2.0.0",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.typelevel" %% "cats-effect" % "2.0.0",

  // Test
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,

  // Specific
  "org.apache.kafka" % "kafka-clients" % "2.4.0",
  "org.rogach" %% "scallop" % "3.3.1",
  "org.jsoup" % "jsoup" % "1.12.1",
  "org.apache.parquet" % "parquet-avro" % "1.11.0",
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion,
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % hadoopVersion
)

dependencyOverrides ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.apache.avro" % "avro" % "1.9.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.8",
  "org.codehaus.jackson" % "jackson-jaxrs" % "1.9.13",
  "org.codehaus.jackson" % "jackson-xc" % "1.9.13",
  "org.apache.commons" % "commons-lang3" % "3.8.1",
  "org.apache.commons" % "commons-compress" % "1.19",
  "io.netty" % "netty" % "3.7.0.Final",
  "com.google.code.findbugs" % "jsr305" % "3.0.0"
)

AvroConfig / sourceDirectory := file("src/main/resources/schema/avro")