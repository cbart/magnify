package magnify.web.api.routes

import akka.util.duration._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ControlTest extends RouteTestBase {
  val route = new Control(system).apply

  test("route should shutdown the actor system") {
    Get("/control/stop") ~> route
    system.awaitTermination(2.seconds)
  }
}
