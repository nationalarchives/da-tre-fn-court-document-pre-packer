package uk.gov.nationalarchives.tre

import play.api.libs.json.{JsArray, JsNull, JsObject, JsString, JsValue, Json}

import scala.collection.immutable.ListMap

object MetadataConstructionUtils {
  def buildMetadataFileContents(
    reference: String,
    fileNames: Seq[String],
    metadataFileName: String,
    parserMetadata: JsObject,
    parserOutputs: JsObject,
    tdrOutputs: JsObject,
    checkSumContent: Option[String],
    fileReference: Option[String] = None
  ): String = {
    val xmlFileName: JsValue = parserOutputs.value.getOrElse("xml", JsNull)
    val logFileName: JsValue = parserOutputs.value.getOrElse("log", JsNull)

    val images = arrayFromField("images")(parserOutputs)
    val errors = arrayFromField("error-messages")(parserOutputs)

    val coreParameters = Json.obj(
        "TRE" -> Json.obj(
          "reference" -> s"TRE-$reference",
          "payload" -> Json.obj(
            "filename" -> getFileNameWithSuffix(".docx")(fileNames),
            "xml" -> xmlFileName,
            "metadata" -> JsString(metadataFileName),
            "images" -> images,
            "log" -> logFileName
          )
        ),
        "PARSER" -> (parserMetadata + ("error-messages" -> errors))
      )

    val additionalTDRData: Seq[(String, JsValue)] =
      Seq(("Document-Checksum-sha256", checkSumContent.map(JsString).getOrElse(JsNull))) ++
        fileReference.map(r => ("File-Reference", JsString(r))).toSeq
    val withTdrSection = if (tdrOutputs.keys.nonEmpty)
      coreParameters + ("TDR" -> (tdrOutputs ++ JsObject(additionalTDRData)))
    else coreParameters
    Json.prettyPrint(Json.obj("parameters" -> withTdrSection))
  }

  def getFileNameWithSuffix(suffix: String): Seq[String] => JsValue = fileNames =>
    fileNames.flatMap(_.split('/').lastOption).find(_.endsWith(suffix)).map(JsString).getOrElse(JsNull)

  def asJson(str: Option[String]): JsObject = str.map(s => Json.parse(s).as[JsObject]).getOrElse(Json.obj())

  /**
   * TDR metadata is currently supplied in a text file with colon separated key value pairs on separate lines
   */
  def textFileStringToJson(str: Option[String]): JsObject = str.map { s =>
    val pairs = s.split("\n").collect { l =>
      l.split(": ").toSeq match {
        case Seq(key: String) if key.nonEmpty => (key -> JsNull)
        case Seq(key: String, value: String) => (key -> JsString(value))
      }
    }
    Json.toJson(ListMap(pairs: _*)).as[JsObject]
  }.getOrElse(Json.obj())

  def csvStringToFileMetadata(str: Option[String]): Seq[FileMetadata] = str.map { s =>
    s.split("\n").tail.flatMap { line =>
      val fields = line.split(',')
      for {
        fileReference <- fields.headOption
        fileName <- fields.lift(1)
      } yield FileMetadata(fileName, fileReference)
    }
  }.toSeq.flatten

  def arrayFromField(fieldName: String): JsObject => JsArray =
    _.value.get(fieldName).collect { case ja: JsArray => ja }.getOrElse(Json.arr())
}

case class FileMetadata(fileName: String, fileReference: String)
