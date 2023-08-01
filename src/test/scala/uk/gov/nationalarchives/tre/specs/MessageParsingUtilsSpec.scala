package uk.gov.nationalarchives.tre.specs

import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.scalatest.flatspec._
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Parameters, Status}
import uk.gov.nationalarchives.tre.MessageParsingUtils._

class MessageParsingUtilsSpec extends AnyFlatSpec with MockitoSugar {

  "The message parsing utils" should "parse a valid tre message" in {
    val testProperties = Properties(
      messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
      timestamp = "2023-01-01T00:00:00.000000Z",
      producer = Producer.TRE,
      function = "test-tre-court-document-pack-lambda",
      executionId = "execution-id",
      parentExecutionId = Some("parent-execution-id")
    )
    val testParameters = Parameters(
      status = Status.COURT_DOCUMENT_PARSE_NO_ERRORS,
      reference = "TRE-TEST",
      s3Bucket = "test-tre-court-document-pack-out",
      s3Key = "TRE-TEST/execution-id/TRE-TEST.tar.gz",
      originator = Some("TRE"),
      metadataFilePath = "/metadata.json",
      metadataFileType = "Json"
    )
    val testMessage = CourtDocumentPackageAvailable(testProperties, testParameters)

    parseCourtDocumentPackageAvailableMessage(testMessage.asJson.toString) shouldBe testMessage
  }
}
