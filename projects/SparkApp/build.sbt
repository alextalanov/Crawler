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
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

mainClass in assembly := Some("com.gmail.wristylotus.Main")

test in assembly := {}


//Scala Compiler Options
lazy val baseScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
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