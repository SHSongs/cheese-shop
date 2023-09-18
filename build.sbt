ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

val zioVersion = "2.0.15"

lazy val sharedSettings = Seq(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-config" % "4.0.0-RC14",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC14",
      "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC14",
      "dev.zio" %% "zio-config-refined" % "4.0.0-RC14",
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "dev.zio" %% "zio-json" % "0.6.0",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.16",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.8.16",
      "com.softwaremill.sttp.client3" %% "zio" % "3.8.16",
      "com.lihaoyi" %% "os-lib" % "0.9.1",
      "com.lihaoyi" %% "ujson" % "3.0.0"
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "cheese-shop"
  )
  .settings(sharedSettings)

