package uk.gov.nationalarchives.tre

import play.api.libs.json.{JsNull, JsObject, JsString, JsValue, Json}

object MetadataConstructionUtils {
  def buildMetadataFileContents(
    reference: String,
    fileNames: Seq[String],
    metadataFileName: String,
    parserMetadata: Option[String]
  ): String = {
    val parserMetadataJson = parserMetadata.map(m => Json.parse(m).as[JsObject]).getOrElse(JsNull)
    val json = Json.obj(
      "parameters" -> Json.obj(
        "TRE" -> Json.obj(
          "reference" -> reference,
          "payload" -> Json.obj(
            "filename" -> getFileNameWithSuffix(".docx")(fileNames),
            "xml" -> getFileNameWithSuffix(".xml")(fileNames),
            "metadata" -> JsString(metadataFileName),
            "images" -> Json.arr(),
            "log" -> "parser.log"
          )
        ),
        "PARSER" -> parserMetadataJson
      )
    )
    Json.prettyPrint(json)
  }

  def getFileNameWithSuffix(suffix: String): Seq[String] => JsValue = fileNames =>
    fileNames.find(_.endsWith(suffix)).map(JsString).getOrElse(JsNull)
}
