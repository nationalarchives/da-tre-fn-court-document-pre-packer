package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.{LambdaDestinationEvent, SNSEvent, SQSEvent}
import scala.jdk.CollectionConverters._

trait NoopEventHandler[Event] {
  def logEvent(event: Event, logger: LambdaLogger): Unit
}

object SNSEventHandler extends NoopEventHandler[SNSEvent] {
  override def logEvent(snsEvent: SNSEvent, logger: LambdaLogger): Unit = {
    snsEvent.getRecords.asScala.toList match {
      case snsRecord :: Nil => logger.log(s"SNS event received with message: ${snsRecord.getSNS.getMessage}")
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }
}

object DestinationEventHandler extends NoopEventHandler[LambdaDestinationEvent] {
  override def logEvent(lambdaDestinationEvent: LambdaDestinationEvent, logger: LambdaLogger): Unit =
    logger.log(s"Destination event received with payload: ${lambdaDestinationEvent.getRequestPayload.asScala.toString}")
}

object SQSEventHandler extends NoopEventHandler[SQSEvent] {
  override def logEvent(sqsEvent: SQSEvent, logger: LambdaLogger): Unit = {
    logger.log(s"SQS event received with messages: \n${sqsEvent.getRecords.asScala.toList.map(_.getBody).mkString("\n")}")
  }
}
