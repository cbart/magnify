package magnify.web.api.routes

import akka.util.duration._
import cc.spray.http.{HttpMethod, HttpRequest}
import cc.spray.http.HttpMethods._
import cc.spray.test.SprayTest
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class DataTest extends FunSuite with SprayTest with ShouldMatchers with BeforeAndAfterAll {
  val route = new Data(actorSystem).apply

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
    responseContent("/data/projects" @@ POST) should be("zip uploaded")
  }

  private def responseContent(result: RoutingResultWrapper): String = {
    val Some(content) = result.response.content
    new String(content.buffer)
  }

  override protected def afterAll() {
    try {
      actorSystem.shutdown()
    } finally {
      super.afterAll()
    }
  }
}
