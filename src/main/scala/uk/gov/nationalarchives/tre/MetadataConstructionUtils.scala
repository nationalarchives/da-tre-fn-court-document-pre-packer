package uk.gov.nationalarchives.tre

import com.github.tototoshi.csv.CSVReader
import play.api.libs.json.{JsArray, JsNull, JsObject, JsString, JsValue, Json}

import java.io.StringReader
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
    inputFileMetadata: Option[FileMetadata] = None
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
        inputFileMetadata.toSeq.flatMap(fm => Seq(("File-Reference", JsString(fm.fileReference)), ("UUID", JsString(fm.uuid))))
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
    val reader = new StringReader(s)
    CSVReader.open(reader).allWithHeaders.flatMap { row =>
      for {
        fileReference <- row.get("file_reference").filter(_.nonEmpty)
        fileName <- row.get("file_name")
        uuid <- row.get("UUID")
      } yield FileMetadata(fileName, fileReference, uuid)
    }
  }.getOrElse(Seq.empty[FileMetadata])

  def arrayFromField(fieldName: String): JsObject => JsArray =
    _.value.get(fieldName).collect { case ja: JsArray => ja }.getOrElse(Json.arr())
}

case class FileMetadata(fileName: String, fileReference: String, uuid: String)
