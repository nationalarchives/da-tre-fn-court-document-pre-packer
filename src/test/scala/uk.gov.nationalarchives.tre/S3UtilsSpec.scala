package uk.gov.nationalarchives.tre

import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{ListObjectsV2Request, ListObjectsV2Response, S3Object}

class S3UtilsSpec extends AnyFlatSpec with MockitoSugar {

  "getFileNames" should "get file names from a bucket and directory" in {
    val s3Client = mock[S3Client]

    val expectedRequest = ListObjectsV2Request.builder()
      .bucket("test-tre-common-data")
      .prefix("TRE-TEST/execution-id")
      .build()

    val objectListing = ListObjectsV2Response.builder()
      .contents(
        S3Object.builder().key("TRE-TEST/execution-id/eat_2022_1.docx").build(),
        S3Object.builder().key("TRE-TEST/execution-id/TRE-TEST.xml").build(),
        S3Object.builder().key("TRE-TEST/execution-id/parser.log").build(),
        S3Object.builder().key("TRE-TEST/execution-id/metadata.json").build()
      )
      .build()

    when(s3Client.listObjectsV2(expectedRequest)).thenReturn(objectListing)

    val s3Utils = new S3Utils(s3Client)

    val expectedFileNames = Seq("eat_2022_1.docx", "TRE-TEST.xml", "parser.log", "metadata.json")
    val actualFileNames = s3Utils.getFileNames("test-tre-common-data", "TRE-TEST/execution-id")

    expectedFileNames shouldBe actualFileNames
  }
}
