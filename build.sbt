import Dependencies._

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / version := "0.1.0"

val awsVersion = "2.20.79"

lazy val root = (project in file("."))
  .settings(
    name := "da-tre-fn-court-document-pre-packer",
    libraryDependencies ++= Seq(
      lambdaRuntimeInterfaceClient
    ),
    assembly / assemblyOutputPath := file("target/function.jar")
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalatest" %% "scalatest" % "3.2.11" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.16.0" % Test,
  "uk.gov.nationalarchives" % "da-transform-schemas" % "2.5",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.1",
  "com.typesafe.play" %% "play-json" % "2.10.0-RC6",
  "software.amazon.awssdk" % "s3" % awsVersion,
  "software.amazon.awssdk" % "sso" % awsVersion,
  "software.amazon.awssdk" % "ssooidc" % awsVersion,
  "com.jayway.jsonpath" % "json-path" % "2.6.0",
  "com.github.tototoshi" %% "scala-csv" % "1.3.10"
)

val circeVersion = "0.14.2"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeVersion)
