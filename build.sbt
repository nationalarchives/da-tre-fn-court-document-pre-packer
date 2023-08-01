import Dependencies._

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "da-tre-fn-template",
    libraryDependencies ++= Seq(
      lambdaRuntimeInterfaceClient
    ),
    assembly / assemblyOutputPath := file("target/function.jar")
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "io.cucumber" %% "cucumber-scala" % "8.14.2" % Test,
  "io.cucumber" % "cucumber-junit" % "7.11.2" % Test,
  "io.cucumber" % "cucumber-core" % "7.11.1" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalatest" %% "scalatest" % "3.2.11" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.16.0" % Test,
  "uk.gov.nationalarchives" % "da-transform-schemas" % "2.3",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.1",
  "com.typesafe.play" %% "play-json" % "2.10.0-RC6")

val circeVersion = "0.14.2"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeVersion)
