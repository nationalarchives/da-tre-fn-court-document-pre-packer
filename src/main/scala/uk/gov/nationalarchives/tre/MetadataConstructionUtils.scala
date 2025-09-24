package uk.gov.nationalarchives.tre

import com.github.tototoshi.csv.CSVReader
import play.api.libs.json._
import uk.gov.nationalarchives.tdr.schema.generated.BaseSchema._
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils

import java.io.StringReader
import scala.collection.immutable.ListMap

object MetadataConstructionUtils {

  private val metadataCSVFieldsForFCLExport: Seq[String] = Seq(
    file_reference,
    UUID,
    judgment_type,
    judgment_update,
    judgment_update_type,
    judgment_update_details,
    judgment_neutral_citation,
    judgment_no_neutral_citation,
    judgment_reference
  )

  private val csvHeaderToFCLKeyMappr = ConfigUtils.loadConfiguration.propertyToOutputMapper("fclExport")

  def buildMetadataFileContents(
    reference: String,
    fileNames: Seq[String],
    metadataFileName: String,
    parserMetadata: JsObject,
    parserOutputs: JsObject,
    tdrOutputs: JsObject,
    checkSumContent: Option[String],
    inputFileMetadata: Seq[FCLExportValue] = List.empty[FCLExportValue]
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
        inputFileMetadata.map(fm => (fm.key, if(fm.value.isEmpty) JsNull else JsString(fm.value)))
    val withTdrSection = if (tdrOutputs.keys.nonEmpty)
      coreParameters + ("TDR" -> (tdrOutputs ++ JsObject(additionalTDRData)))
    else coreParameters
    Json.prettyPrint(Json.obj("parameters" -> withTdrSection))
  }



  def getFileNameWithSuffix(suffix: String): Seq[String] => JsValue = fileNames =>
    fileNames.flatMap(_.split('/').lastOption).find(_.endsWith(suffix)).map(JsString).getOrElse(JsNull)

  private def arrayFromField(fieldName: String): JsObject => JsArray =
    _.value.get(fieldName).collect { case ja: JsArray => ja }.getOrElse(Json.arr())

  def asJson(str: Option[String]): JsObject = str.map(s => Json.parse(s).as[JsObject]).getOrElse(Json.obj())

  /**
   * TDR metadata is currently supplied in a text file with colon : separated key value pairs on separate lines
   */
  def textFileStringToJson(str: Option[String]): JsObject = str.map { s =>
    val pairs = s.split("\n").collect { l =>
      l.split(": ").toSeq match {
        case Seq(key: String) if key.nonEmpty => key -> JsNull
        case Seq(key: String, value: String) => key -> JsString(value)
      }
    }
    Json.toJson(ListMap.from(pairs)).as[JsObject]
  }.getOrElse(Json.obj())

 def csvStringToRequiredCSVValues(str: Option[String], inputFileName: Option[String]): Seq[FCLExportValue] = {
    str.flatMap { s =>
      val reader = new StringReader(s)
      val rows: Seq[Map[String, String]] = CSVReader.open(reader).allWithHeaders()

      rows.filter(csvValueMap => csvValueMap.getOrElse("file_reference", "").nonEmpty).find { csvValueMap =>
        csvValueMap.get("file_name").exists { fileName =>
          inputFileName.exists(_.endsWith(fileName))
        }
      }.map { row =>
        metadataCSVFieldsForFCLExport.map { field =>
          FCLExportValue(csvHeaderToFCLKeyMappr(field), row.getOrElse(field, ""))
        }
      }
    }.getOrElse(Seq.empty[FCLExportValue])
  }
}

case class FCLExportValue(key:String, value: String)
