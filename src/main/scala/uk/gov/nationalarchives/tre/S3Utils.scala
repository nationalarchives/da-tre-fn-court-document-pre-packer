package uk.gov.nationalarchives.tre

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{CopyObjectRequest, CopyObjectResponse, DeleteObjectRequest, DeleteObjectResponse, GetObjectRequest, ListObjectsV2Request, ListObjectsV2Response, PutObjectRequest}

import java.io.{BufferedReader, InputStreamReader}
import scala.jdk.CollectionConverters._

class S3Utils(s3Client: S3Client) {

  def getFileNames(bucketName: String, directory: String): Seq[String] = {
    val listObjectsRequest = ListObjectsV2Request.builder()
      .bucket(bucketName)
      .prefix(directory)
      .build()

    val listObjectsResponse: ListObjectsV2Response = s3Client.listObjectsV2(listObjectsRequest)

    listObjectsResponse.contents().asScala.map(_.key()).toSeq
      .map(_.replace(s"$directory/", ""))
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
