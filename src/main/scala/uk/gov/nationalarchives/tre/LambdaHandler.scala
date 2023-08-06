package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import uk.gov.nationalarchives.tre.MessageParsingUtils._
import uk.gov.nationalarchives.tre.MetadataConstructionUtils._

import uk.gov.nationalarchives.tre.messages.courtdocument.parse.CourtDocumentParse

import scala.jdk.CollectionConverters.CollectionHasAsScala

class LambdaHandler extends RequestHandler[SNSEvent, String] {
  val s3Utils = new S3Utils(S3Client.builder().region(Region.EU_WEST_2).build())

  override def handleRequest(event: SNSEvent, context: Context): String = {
    event.getRecords.asScala.toList match {
      case snsRecord :: Nil =>
        context.getLogger.log(s"Received SNS message: ${snsRecord.getSNS.getMessage}\n")
        val courtDocumentParseMessage = parseCourtDocumentParseMessage(snsRecord.getSNS.getMessage)
        context.getLogger.log(s"Successfully parsed incoming message as CourtDocumentParse\n")
        buildTREMetadataFileAndUploadToS3(courtDocumentParseMessage, s3Utils)
        context.getLogger.log(s"Successfully uploaded TRE metadata file to S3\n")
        val prepareMessage = courtDocumentPackagePrepareJsonString(courtDocumentParseMessage)
        context.getLogger.log(s"Returning court document prepare message: $prepareMessage\n")
        prepareMessage
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }

  private def buildTREMetadataFileAndUploadToS3(
    courtDocumentParseMessage: CourtDocumentParse,
    s3Utils: S3Utils
  ): Unit = {
    import courtDocumentParseMessage.parameters._
    val metadataFileName = s"TRE-$reference-metadata.json"
    val fileNames = s3Utils.getFileNames(s3Bucket, s3FolderName)
    val parserMetadata = s3Utils.getFileContent(s3Bucket, s"$s3FolderName/metadata.json")
    val metadataFileContent = buildMetadataFileContents(reference, fileNames, metadataFileName, parserMetadata)
    s3Utils.saveStringToFile(metadataFileContent, s3Bucket, s"$s3FolderName/$metadataFileName")
  }
}
