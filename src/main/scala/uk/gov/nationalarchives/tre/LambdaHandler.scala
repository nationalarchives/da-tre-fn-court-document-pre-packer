package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import play.api.libs.json.JsArray
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
        val outDirectory = populateOutDirectory(courtDocumentParseMessage, s3Utils)
        context.getLogger.log(s"Successfully populated out directory\n")
        val prepareMessage = courtDocumentPackagePrepareJsonString(courtDocumentParseMessage, outDirectory = outDirectory)
        context.getLogger.log(s"Returning court document prepare message: $prepareMessage\n")
        prepareMessage
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }

  /**
   * Builds a TRE specific metadata file and places it into an "out" directory alongside other key files produced by
   * the parser
   * @return The directory into which files have been placed ready for packing
   */
  private def populateOutDirectory(
    courtDocumentParseMessage: CourtDocumentParse,
    s3Utils: S3Utils
  ): String = {
    import courtDocumentParseMessage.parameters._
    val metadataFileName = s"TRE-$reference-metadata.json"
    val fileNames = s3Utils.getFileNames(s3Bucket, s3FolderName)

    val fileContentFromS3: String => Option[String] = fileName => if (fileNames.contains(fileName))
      Some(s3Utils.getFileContent(s3Bucket, s"$s3FolderName/$fileName"))
    else None

    val parserMetadata = asJson(fileContentFromS3("metadata.json"))
    val parserOutputs = asJson(fileContentFromS3("parser-outputs.json"))
    val tdrOutputs = textFileStringToJson(fileContentFromS3("bag-info.txt"))
    val checkSumFileContent = fileContentFromS3("manifest-sha256.txt").flatMap(_.split(" ").headOption)

    val metadataFileContent =
      buildMetadataFileContents(reference, fileNames, metadataFileName, parserMetadata, parserOutputs, tdrOutputs, checkSumFileContent)

    val toPackDirectory = s"$s3FolderName/out"
    s3Utils.saveStringToFile(metadataFileContent, s3Bucket, s"$toPackDirectory/$metadataFileName")

    val images = parserOutputs.value.get("images") match {
      case Some(jsArray: JsArray) => jsArray.as[Seq[String]]
      case _ => Seq.empty[String]
    }

    val filesToPack = Seq(
      s"$reference.xml",
      "parser.log"
    ) ++ images

    val isInputFile: String => Boolean = s => s.startsWith("data/") && s.endsWith("docx")
    fileNames.filter(n => filesToPack.contains(n) || isInputFile(n)).foreach { fileName =>
      s3Utils.copyFile(
        fromBucket = s3Bucket,
        toBucket = s3Bucket,
        fromKey = s"$s3FolderName/$fileName",
        toKey = s"$toPackDirectory/$fileName"
      )
    }
    toPackDirectory
  }
}
