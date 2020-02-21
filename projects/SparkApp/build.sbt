lazy val base = (project in file("."))
  .settings(
    name := "CountriesReview",
    version := "0.1",
    scalaVersion := "2.12.10",
    libraryDependencies ++= baseDependencies,
    scalacOptions ++= baseScalacOptions
  )

lazy val baseDependencies = Seq(
  //Common
  "org.apache.spark" %% "spark-sql" % "2.4.4" % "provided",
  "org.jsoup" % "jsoup" % "1.12.1",
  "org.rogach" %% "scallop" % "3.3.1",

  //Test
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val baseScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlint"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case _ => MergeStrategy.singleOrError
}