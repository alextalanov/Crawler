import Dependencies.{spark, common, testing}

//Projects
lazy val base = (project in file("."))
  .settings(
    name := "CountriesReview",
    version := "0.1",
    scalaVersion := "2.11.12",
    libraryDependencies ++= spark ++ common ++ testing,
    scalacOptions ++= baseScalacOptions,
    clearTask
  )


//Assembly
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case PathList(xs@_*) if List(".txt", ".html").exists(xs.last.endsWith) => MergeStrategy.discard
  case PathList("org", "apache", "spark", "unused", _*) => MergeStrategy.discard
  case PathList("org", "apache", "commons", _*) => MergeStrategy.last
  case _ => MergeStrategy.deduplicate
}

assembly / test := {}


//Scala Compiler Options
lazy val baseScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xmax-classfile-name", "240",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-Ypartial-unification",
  "-Xfuture",
  "-Xlint"
)


//Custom tasks
lazy val clear = taskKey[Unit]("Clear std::out")
lazy val clearTask = clear := {
  import scala.language.postfixOps
  import sys.process._
  "bash -c clear" !
}