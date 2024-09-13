import Dependencies._

ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0"

val awsVersion = "2.27.22"

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
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "uk.gov.nationalarchives" % "da-transform-schemas" % "2.8",
  "com.amazonaws" % "aws-lambda-java-events" % "3.13.0",
  "org.playframework" %% "play-json" % "3.0.4",
  "software.amazon.awssdk" % "s3" % awsVersion,
  "software.amazon.awssdk" % "sso" % awsVersion,
  "software.amazon.awssdk" % "ssooidc" % awsVersion,
  "com.jayway.jsonpath" % "json-path" % "2.9.0",
  "com.github.tototoshi" %% "scala-csv" % "2.0.0",
  "io.circe" %% "circe-generic-extras" % "0.14.4"
)

val circeVersion = "0.14.10"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
