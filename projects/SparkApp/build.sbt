lazy val base = (project in file("."))
  .settings(
    name := "SparkApp",
    version := "0.1",
    scalaVersion := "2.12.10",
    libraryDependencies ++= baseDependencies
  )

lazy val baseDependencies = Seq(
  "org.apache.spark" %% "spark-sql" % "2.4.4" % "provided"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case _ => MergeStrategy.first
}