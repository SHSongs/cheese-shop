ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

val zioVersion = "2.0.15"

lazy val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "cheese-shop"
  )

lazy val `cheese-shop` = project
  .settings(sharedSettings)
