package uk.gov.nationalarchives.tre.steps

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.api.client.logging.{LambdaContextLogger, StdOutLogSink}
import com.jayway.jsonpath.JsonPath
import io.cucumber.datatable.DataTable
import io.cucumber.scala.{EN, ScalaDsl}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.tre.LambdaHandler
import uk.gov.nationalarchives.tre.TestHelpers._

import scala.jdk.CollectionConverters.CollectionHasAsScala

class StepDefinitions extends ScalaDsl with EN
    with MockitoSugar {

    val mockContext: Context = mock[Context]
    when(mockContext.getLogger).thenReturn(new LambdaContextLogger(new StdOutLogSink))

    val testContext = new TestContext

    When("an SNS event is received with message content:") { (data: String) =>
        testContext.setSNSData(data)
    }

    Then("a message is returned containing json data:") { (data: DataTable) =>
        val lambdaHandler = new LambdaHandler
        val returnedMessage = lambdaHandler.handleRequest(snsEvent(testContext.getSNSData), mockContext)
        checkAgainst(data)(returnedMessage) shouldBe true
    }

    def checkAgainst(expectedKeysAndValues: DataTable): String => Boolean = { jsonString =>
        expectedKeysAndValues.cells().asScala.map(_.asScala.toSeq).flatMap { row =>
            for {
                path <- row.headOption
                value <- row.lift(1)
            } yield valueAt(path)(jsonString) == value
        }.forall(identity)
    }

    def valueAt(path: String): String => String = jsonString => JsonPath.read[String](jsonString, path)

}

class TestContext {
    var snsData: Option[String] = None
    def setSNSData(data: String): Unit = snsData = Some(data)

    def getSNSData: String = snsData.getOrElse(throw new RuntimeException("Expected context sns data not set"))
}