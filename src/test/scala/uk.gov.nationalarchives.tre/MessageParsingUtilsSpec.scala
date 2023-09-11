package uk.gov.nationalarchives.tre

import io.circe.syntax.EncoderOps
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.common.messages.Status.COURT_DOCUMENT_PARSE_NO_ERRORS
import uk.gov.nationalarchives.common.messages.{CourtDocumentPackageParameters, Producer, Properties}
import uk.gov.nationalarchives.tre.MessageParsingUtils._
import uk.gov.nationalarchives.tre.messages.courtdocument.parse.CourtDocumentParse

import java.time.Instant
import java.util.UUID

class MessageParsingUtilsSpec extends AnyFlatSpec with MockitoSugar {

  val testProperties: Properties = Properties(
    messageType = "uk.gov.nationalarchives.tre.messages.courtdocument.parse,CourtDocumentParse",
    timestamp = "2023-01-01T00:00:00.000000Z",
    producer = Producer.TRE,
    function = "da-tre-fn-court-document-parse",
    executionId = "execution-id",
    parentExecutionId = Some("parent-execution-id")
  )
  val testParameters: CourtDocumentPackageParameters = CourtDocumentPackageParameters(
    reference = "TRE-TEST",
    s3Bucket = "test-tre-common-data",
    s3FolderName = "TRE-TEST/execution-id",
    originator = Some("TRE"),
    status = COURT_DOCUMENT_PARSE_NO_ERRORS
  )
  val courtDocumentParse: CourtDocumentParse = CourtDocumentParse(testProperties, testParameters)

  "parseCourtDocumentParseMessage" should "parse a valid CourtDocumentParse json string" in {
    val courtDocumentParseJsonString =
      """
       {
          "properties": {
            "messageType": "uk.gov.nationalarchives.tre.messages.courtdocument.parse,CourtDocumentParse",
            "timestamp": "2023-01-01T00:00:00.000000Z",
            "producer": "TRE",
            "function": "da-tre-fn-court-document-parse",
            "executionId": "execution-id",
            "parentExecutionId": "parent-execution-id"
          },
          "parameters": {
             "reference": "TRE-TEST",
             "s3Bucket": "test-tre-common-data",
             "s3FolderName": "TRE-TEST/execution-id",
             "originator": "TRE",
             "status": "COURT_DOCUMENT_PARSE_NO_ERRORS"
          }
      }
      """
    parseCourtDocumentParseMessage(courtDocumentParseJsonString) shouldBe courtDocumentParse
  }

  "courtDocumentPackagePrepareJsonString" should "produce a json string from a valid CourtDocumentPackagePrepare case class, specifying the out directory for packing" in {
    val testUUID = UUID.randomUUID().toString
    val testTimestamp = Instant.now().toString
    val courtDocumentPackagePrepareJsonString =
      s"""{
         |  "properties" : {
         |    "messageType" : "uk.gov.nationalarchives.tre.messages.courtdocumentpackage.prepare.CourtDocumentPackagePrepare",
         |    "timestamp" : "$testTimestamp",
         |    "function" : "da-tre-fn-court-document-pre-packer",
         |    "producer" : "TRE",
         |    "executionId" : "$testUUID",
         |    "parentExecutionId" : "execution-id"
         |  },
         |  "parameters" : {
         |    "originator" : "TRE",
         |    "s3FolderName" : "TRE-TEST/execution-id/out",
         |    "s3Bucket" : "test-tre-common-data",
         |    "reference" : "TRE-TEST",
         |    "status" : "COURT_DOCUMENT_PARSE_NO_ERRORS"
         |  }
         |}""".stripMargin
    MessageParsingUtils.courtDocumentPackagePrepareJsonString(courtDocumentParse, testUUID,  testTimestamp, outDirectory = "TRE-TEST/execution-id/out") shouldBe courtDocumentPackagePrepareJsonString
  }
}
