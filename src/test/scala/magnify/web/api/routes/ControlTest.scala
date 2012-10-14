package magnify.web.api.routes

import akka.util.duration._
import cc.spray.http.{HttpMethods, HttpRequest}
import cc.spray.test.SprayTest
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ControlTest extends FunSuite with SprayTest with ShouldMatchers {
  val route = new Control(actorSystem).get

  test("route should shutdown the actor system") {
    test(HttpRequest(HttpMethods.GET, "/control/stop"), 1.second)(route)
    actorSystem.awaitTermination(2.seconds)
    actorSystem should be('terminated)
  }
}
