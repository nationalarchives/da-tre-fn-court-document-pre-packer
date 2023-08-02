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
}
