package magnify.web.api.routes

import akka.util.duration._
import cc.spray.http.{HttpMethod, HttpRequest}
import cc.spray.http.HttpMethods._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar._
import magnify.web.api.controllers.DataController
import org.mockito.Mockito.when
import org.mockito.Matchers._
import akka.testkit.TestKit
import akka.actor.ActorSystem
import magnify.testing.{SpraySuite, ActorsSuite}
import cc.spray.RequestContext
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import cc.spray.typeconversion.DefaultMarshallers._

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class DataTest extends TestKit(ActorSystem()) with FunSuite with ActorsSuite with SpraySuite
    with ShouldMatchers {
  val controller = mock[DataController]

  val route = new Data(system, controller).apply

  implicit def request(path: String) = new {
    def @@(method: HttpMethod): RoutingResultWrapper =
      test(HttpRequest(method, path), 50.millis)(route)
  }

  test("should render projects list") {
    responseContent("/data/projects/list.json" @@ GET) should be("LIST.JSON")
  }

  test("should render whole.gexf") {
    responseContent("/data/projects/0/head/whole.gexf" @@ GET) should be("whole.gexf for project 0")
  }

  test("should respond to calls post") {
    responseContent("/data/projects/0/head/calls" @@ POST) should be("uploaded calls.csv for 0")
  }

  test("should respond to zip post") {
    when(controller.uploadSources(any(classOf[RequestContext]))).thenAnswer(new Answer[Void] {
      def answer(invocation: InvocationOnMock) = {
        val requestContext = invocation.getArguments().head.asInstanceOf[RequestContext]
        requestContext.complete("TEST RESPONSE")
        null
      }
    })
    responseContent("/data/projects" @@ POST) should be("TEST RESPONSE")
  }

  private def responseContent(result: RoutingResultWrapper): String = {
    val Some(content) = result.response.content
    new String(content.buffer)
  }

  //private def when[T](mockInvocation: => T) = new {
  //  def then(answer)
  //}
}
