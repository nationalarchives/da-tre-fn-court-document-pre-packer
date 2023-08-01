package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.{LambdaDestinationEvent, SNSEvent, SQSEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class LambdaHandler[Event]() extends RequestHandler[Event, Unit] {

  override def handleRequest(event: Event, context: Context): Unit = {
    event match {
      case snsEvent: SNSEvent => SNSEventHandler.logEvent(snsEvent, context.getLogger)
      case lambdaDestinationEvent: LambdaDestinationEvent => DestinationEventHandler.logEvent(lambdaDestinationEvent, context.getLogger)
      case sqsEvent: SQSEvent => SQSEventHandler.logEvent(sqsEvent, context.getLogger)
      case _=> throw new NotImplementedError(s"Unrecognised lambda event")
    }
  }
}
