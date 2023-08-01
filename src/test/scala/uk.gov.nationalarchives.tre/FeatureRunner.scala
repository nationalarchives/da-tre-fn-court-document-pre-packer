import io.cucumber.junit.{Cucumber, CucumberOptions}
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("classpath:features/"),
  glue = Array("classpath:uk.gov.nationalarchives.tre.steps"),
  plugin = Array("pretty", "html:target/cucumber/tests.html"))
class FeatureRunner {}
