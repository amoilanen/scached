import Dependencies._

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val AkkaVersion = "2.6.16"
val ZioVersion = "2.0.0-M4"

lazy val root = (project in file("."))
  .settings(
    name := "scached",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.scalamock" %% "scalamock" % "5.1.0" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "dev.zio" %% "zio" % ZioVersion
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
