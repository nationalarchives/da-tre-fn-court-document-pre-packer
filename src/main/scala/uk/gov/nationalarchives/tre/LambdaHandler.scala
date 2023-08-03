package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import MessageParsingUtils._
import io.circe.syntax.EncoderOps
import play.api.libs.json.{JsObject, JsString, Json}
import steps.S3Utils.{closeClient, getFileContent, getFileNames, saveStringToFile}
import uk.gov.nationalarchives.common.messages.Producer.TRE
import uk.gov.nationalarchives.common.messages.Properties
import uk.gov.nationalarchives.tre.messages.courtdocumentpackage.prepare.CourtDocumentPackagePrepare

import java.time.Instant
import java.util.UUID
import scala.jdk.CollectionConverters.CollectionHasAsScala

class LambdaHandler extends RequestHandler[SNSEvent, String] {

  //TODO: Refactor so this is a bit nicer. Remove feature file and unit test instead. Add system testing feature file
  override def handleRequest(event: SNSEvent, context: Context): String = {
    event.getRecords.asScala.toList match {
      case snsRecord :: Nil => {
        try {
          val courtDocumentParseMessage = parseCourtDocumentParseMessage(snsRecord.getSNS.getMessage)
          import courtDocumentParseMessage.parameters._
          val metadataFileName = s"TRE-$reference-metadata.json"
          val fileNames = getFileNames(s3Bucket, s3FolderName)
          val parserMetadata = getFileContent(s3Bucket, s"$s3FolderName/metadata.json")

          def getFileNameWithSuffix(suffix: String): String =
            fileNames.find(_.endsWith(suffix)).map(_.replace(s"$s3FolderName/", "")).getOrElse("null")
          val metadataFileContent = buildMetadataFileContents(
            reference = reference,
            docxFileName = getFileNameWithSuffix(".docx"),
            xmlFileName = getFileNameWithSuffix(".xml"),
            metadataFileName = metadataFileName,
            parserMetadata = parserMetadata
          )
          saveStringToFile(content = metadataFileContent, bucketName = s3Bucket, key = s"$s3FolderName/$metadataFileName")
          val returnString = CourtDocumentPackagePrepare(
            properties = Properties(
              messageType = "uk.gov.nationalarchives.tre.messages.courtdocumentpackage.prepare.CourtDocumentPackagePrepare",
              timestamp = Instant.now.toString,
              function = "da-tre-fn-court-document-pre-packer",
              producer = TRE,
              executionId = UUID.randomUUID().toString,
              parentExecutionId = Some(courtDocumentParseMessage.properties.executionId)
            ),
            parameters = courtDocumentParseMessage.parameters.copy(s3FolderName = s"$s3FolderName/")
          ).asJson.toString()
          returnString
        } catch {
          case e: Throwable => throw e
        } finally {
          closeClient()
        }
      }
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }

  def buildMetadataFileContents(
    reference: String,
    docxFileName: String,
    xmlFileName: String,
    metadataFileName: String,
    parserMetadata: String
  ): String = {
    val parserMetadataJson = Json.parse(parserMetadata).as[JsObject]
    val json = Json.obj(
      "parameters" -> Json.obj(
        "TRE" -> Json.obj(
          "reference" -> reference,
          "payload" -> Json.obj(
            "filename" -> JsString(docxFileName),
            "xml" -> JsString(xmlFileName),
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
}
