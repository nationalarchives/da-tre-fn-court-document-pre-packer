import Dependencies._

ThisBuild / scalaVersion := "2.13.17"
ThisBuild / version := "0.1.0"

val awsVersion = "2.37.3"

lazy val root = project.in(file("."))
  .settings(
    name := "da-tre-fn-court-document-pre-packer",
    libraryDependencies ++= Seq(
      lambdaRuntimeInterfaceClient
    ),
    assembly / assemblyOutputPath := file("target/function.jar")
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "uk.gov.nationalarchives" % "da-transform-schemas" % "2.14",
  "uk.gov.nationalarchives" %% "da-metadata-schema" % "0.0.96",
  "com.amazonaws" % "aws-lambda-java-events" % "3.16.1",
  "org.playframework" %% "play-json" % "3.0.6",
  "software.amazon.awssdk" % "s3" % awsVersion,
  "software.amazon.awssdk" % "sso" % awsVersion,
  "software.amazon.awssdk" % "ssooidc" % awsVersion,
  "com.jayway.jsonpath" % "json-path" % "2.9.0",
  "com.github.tototoshi" %% "scala-csv" % "2.0.0",
  "io.circe" %% "circe-generic-extras" % "0.14.4"
)

val circeVersion = "0.14.15"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
