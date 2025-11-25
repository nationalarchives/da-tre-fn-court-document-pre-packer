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
      .prefix("TRE-TEST/execution-id/")
      .maxKeys(1000)
      .build()

    val objectListing = ListObjectsV2Response.builder()
      .contents(
        S3Object.builder().key("TRE-TEST/execution-id/eat_2022_1.docx").build(),
        S3Object.builder().key("TRE-TEST/execution-id/TRE-TEST.xml").build(),
        S3Object.builder().key("TRE-TEST/execution-id/parser.log").build(),
        S3Object.builder().key("TRE-TEST/execution-id/metadata.json").build()
      )
      .isTruncated(false)
      .build()

    when(s3Client.listObjectsV2(expectedRequest)).thenReturn(objectListing)

    val s3Utils = new S3Utils(s3Client)

    val expectedFileNames = Seq("eat_2022_1.docx", "TRE-TEST.xml", "parser.log", "metadata.json")
    val actualFileNames = s3Utils.getFileNames("test-tre-common-data", "TRE-TEST/execution-id")

    expectedFileNames shouldBe actualFileNames
  }

  "getFileNames" should "paginate across multiple pages" in {
    val s3Client = mock[S3Client]
    val bucket = "test-tre-common-data"
    val prefixNoSlash = "TRE-TEST/execution-id"
    val prefix = prefixNoSlash + "/"

    val firstRequest = ListObjectsV2Request.builder()
      .bucket(bucket)
      .prefix(prefix)
      .maxKeys(1000)
      .build()

    val secondRequest = ListObjectsV2Request.builder()
      .bucket(bucket)
      .prefix(prefix)
      .maxKeys(1000)
      .continuationToken("token-1")
      .build()

    val firstResponse = ListObjectsV2Response.builder()
      .contents(
        S3Object.builder().key(prefix + "file1.txt").build(),
        S3Object.builder().key(prefix + "file2.txt").build()
      )
      .isTruncated(true)
      .nextContinuationToken("token-1")
      .build()

    val secondResponse = ListObjectsV2Response.builder()
      .contents(
        S3Object.builder().key(prefix + "file3.txt").build(),
        S3Object.builder().key(prefix + "file4.txt").build()
      )
      .isTruncated(false)
      .build()

    when(s3Client.listObjectsV2(firstRequest)).thenReturn(firstResponse)
    when(s3Client.listObjectsV2(secondRequest)).thenReturn(secondResponse)

    val s3Utils = new S3Utils(s3Client)
    val files = s3Utils.getFileNames(bucket, prefixNoSlash)

    files shouldBe Seq("file1.txt", "file2.txt", "file3.txt", "file4.txt")
  }
}
