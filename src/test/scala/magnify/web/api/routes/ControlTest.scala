package magnify.web.api.routes

import akka.util.duration._
import cc.spray.http.{HttpMethods, HttpRequest}
import cc.spray.test.SprayTest
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ControlTest extends FunSuite with SprayTest with ShouldMatchers with BeforeAndAfterAll {
  val route = new Control(actorSystem).apply

  test("route should shutdown the actor system") {
    test(HttpRequest(HttpMethods.GET, "/control/stop"), 50.millis)(route)
    actorSystem.awaitTermination(2.seconds)
    actorSystem should be('terminated)
  }

  protected override def afterAll() {

  }
}
