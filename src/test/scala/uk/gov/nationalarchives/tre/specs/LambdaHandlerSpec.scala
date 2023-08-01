package uk.gov.nationalarchives.tre.specs

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.api.client.logging.{LambdaContextLogger, LogSink}
import com.amazonaws.services.lambda.runtime.events.{LambdaDestinationEvent, SNSEvent, SQSEvent}
import org.mockito.Mockito.when
import org.scalatest.flatspec._
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.tre.TestHelpers.{LogHolder, TestLogSink}
import uk.gov.nationalarchives.tre.{LambdaHandler, TestHelpers}

class LambdaHandlerSpec extends AnyFlatSpec with MockitoSugar {
  val mockLambdaContext: Context = mock[Context]
  val logHolder = new LogHolder
  when(mockLambdaContext.getLogger).thenReturn(new LambdaContextLogger(new TestLogSink(logHolder)))

  "The lambda handler" should "log an SNS event" in {
    val lambda = new LambdaHandler[SNSEvent]()
    val snsEvent = TestHelpers.snsEvent("test")

    lambda.handleRequest(snsEvent, mockLambdaContext)
    logHolder.getLatestLog shouldBe "SNS event received with message: test"
  }

  "The lambda handler" should "log an SQS event" in {
    val lambda = new LambdaHandler[SQSEvent]()
    val sqsEvent = TestHelpers.sqsEvent("test")

    lambda.handleRequest(sqsEvent, mockLambdaContext)
    logHolder.getLatestLog shouldBe "SQS event received with messages: \ntest"
  }

  "The lambda handler" should "log a lambda destination event" in {
    val lambda = new LambdaHandler[LambdaDestinationEvent]()
    val lambdaDestinationEvent = TestHelpers.lambdaDestinationEvent(Map("message" -> "test"))

    lambda.handleRequest(lambdaDestinationEvent, mockLambdaContext)
    logHolder.getLatestLog shouldBe s"Destination event received with payload: ${Map("message" -> "test")}"
  }
}
