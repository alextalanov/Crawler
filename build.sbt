name := "Crawler"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  // Common
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.julien-truffaut" %% "monocle-core" % "1.5.1-cats",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.typelevel" %% "cats-effect" % "1.1.0",

  // Test
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",

  // Specific
  "org.rogach" %% "scallop" % "3.3.1"
)