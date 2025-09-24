package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.{SNS, SNSRecord}

import scala.jdk.CollectionConverters.SeqHasAsJava

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
