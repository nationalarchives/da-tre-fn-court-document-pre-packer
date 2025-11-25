package uk.gov.nationalarchives.tre

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._

import java.io.{BufferedReader, InputStreamReader}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

class S3Utils(s3Client: S3Client) {

  def getFileNames(bucketName: String, directory: String): Seq[String] = {
    val prefix = if (directory.endsWith("/")) directory else directory + "/"

    @tailrec
    def loop(token: Option[String], acc: Vector[String]): Vector[String] = {
      val builder = ListObjectsV2Request.builder()
        .bucket(bucketName)
        .prefix(prefix)
        .maxKeys(1000)
      token.foreach(builder.continuationToken)
      val resp = s3Client.listObjectsV2(builder.build())

      val pageKeys = resp.contents().asScala.iterator
        .map(_.key())
        .map(_.substring(prefix.length))
        .filter(_.nonEmpty)
        .toVector

      val nextToken = Option(resp.nextContinuationToken())
      val updated = acc ++ pageKeys // preserve ordering
      if (resp.isTruncated) loop(nextToken, updated) else updated
    }

    loop(None, Vector.empty)
  }

  def getFileContent(bucketName: String, fileKey: String): String = {
    val getObjectRequest = GetObjectRequest.builder()
      .bucket(bucketName)
      .key(fileKey)
      .build()
    val response = s3Client.getObject(getObjectRequest)
    val bufferedReader = new BufferedReader(new InputStreamReader(response))
    val content = Iterator.continually(bufferedReader.readLine()).takeWhile(_ != null).mkString("\n")
    bufferedReader.close()
    content
  }

  def copyFile(fromBucket: String, toBucket: String, fromKey: String, toKey: String): CopyObjectResponse = {
    val copyObjectRequest: CopyObjectRequest = CopyObjectRequest.builder()
      .sourceBucket(fromBucket)
      .sourceKey(fromKey)
      .destinationBucket(toBucket)
      .destinationKey(toKey)
      .build()
    s3Client.copyObject(copyObjectRequest)
  }

  def saveStringToFile(content: String, bucketName: String, key: String): Unit = {
    val putObjectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build()
    s3Client.putObject(putObjectRequest, RequestBody.fromString(content))
  }

}
