package uk.gov.nationalarchives.tre

import play.api.libs.json.{JsArray, JsNull, JsObject, JsString, JsValue, Json}

object MetadataConstructionUtils {
  def buildMetadataFileContents(
    reference: String,
    fileNames: Seq[String],
    metadataFileName: String,
    parserMetadata: JsObject,
    parserOutputs: JsObject
  ): String = {
    val xmlFileName: JsValue = parserOutputs.value.getOrElse("xml", JsNull)
    val logFileName: JsValue = parserOutputs.value.getOrElse("log", JsNull)
    val images = arrayFromField("images")(parserOutputs)
    val errors = arrayFromField("error-messages")(parserOutputs)

    val json = Json.obj(
      "parameters" -> Json.obj(
        "TRE" -> Json.obj(
          "reference" -> reference,
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
    )
    Json.prettyPrint(json)
  }

  def getFileNameWithSuffix(suffix: String): Seq[String] => JsValue = fileNames =>
    fileNames.flatMap(_.split('/').lastOption).find(_.endsWith(suffix)).map(JsString).getOrElse(JsNull)

  def asJson(str: Option[String]): JsObject = str.map(s => Json.parse(s).as[JsObject]).getOrElse(Json.obj())

  def arrayFromField(fieldName: String): JsObject => JsArray =
    _.value.get(fieldName).collect { case ja: JsArray => ja }.getOrElse(Json.arr())

}
