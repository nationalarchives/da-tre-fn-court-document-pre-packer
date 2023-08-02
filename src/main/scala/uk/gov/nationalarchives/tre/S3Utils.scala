package steps

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ListObjectsV2Response, PutObjectRequest}

import java.io.{BufferedReader, InputStreamReader}
import scala.jdk.CollectionConverters._

object S3Utils {
  private lazy val s3Client: S3Client = S3Client.builder().region(Region.EU_WEST_2).build()

  def getFileNames(bucketName: String, directory: String): Seq[String] = {
    val listObjectsRequest = ListObjectsV2Request.builder()
      .bucket(bucketName)
      .prefix(directory)
      .build()

    val listObjectsResponse: ListObjectsV2Response = s3Client.listObjectsV2(listObjectsRequest)

    listObjectsResponse.contents().asScala.map(_.key()).toSeq
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

  def saveStringToFile(content: String, bucketName: String, key: String): Unit = {
    val putObjectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build()
    s3Client.putObject(putObjectRequest, RequestBody.fromString(content))
  }
  def closeClient(): Unit = s3Client.close()
}
