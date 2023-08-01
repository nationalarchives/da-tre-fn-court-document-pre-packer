package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.api.client.logging.LogSink
import com.amazonaws.services.lambda.runtime.events.{LambdaDestinationEvent, SNSEvent, SQSEvent}
import com.amazonaws.services.lambda.runtime.events.SNSEvent.{SNS, SNSRecord}
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import play.api.libs.json.JsString

import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava}

object TestHelpers {
  def snsEvent(message: String): SNSEvent = {
    val record = new SNSRecord;
    val sns = new SNS
    sns.setMessage(message)
    record.setSns(sns)
    val snsEvent = new SNSEvent()
    snsEvent.setRecords(List(record).asJava)
    snsEvent
  }

  def sqsEvent(message: String): SQSEvent = {
    val sqsEvent = new SQSEvent
    val sqsMessage = new SQSMessage
    sqsMessage.setBody(message)
    sqsEvent.setRecords(List(sqsMessage).asJava)
    sqsEvent
  }

  def lambdaDestinationEvent(payload: Map[String, AnyRef]): LambdaDestinationEvent = {
    LambdaDestinationEvent.builder()
      .withRequestPayload(payload.asJava)
      .build()
  }

  class TestLogSink(logHolder: LogHolder) extends LogSink {
    override def log(message: Array[Byte]): Unit = logHolder.setLatestLog(message)

    override def close(): Unit = {}
  }

  class LogHolder {
    var latestLog: Option[String] = None

    def getLatestLog: String = latestLog.getOrElse(throw new RuntimeException("Nothing yet logged"))

    def setLatestLog(message: Array[Byte]): Unit = latestLog = Some(new String(message, StandardCharsets.UTF_8))
  }
}
