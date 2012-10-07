package magnify.web.api.routes

import magnify.core
import magnify.web.api
import magnify.testing.web.WebApiTest

import akka.util.duration._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class ControlTest extends FunSuite with ShouldMatchers with BeforeAndAfterAll with WebApiTest
    with core.Module with api.Module {
  test("GET /control/stop should kill the actor system in less than 2 seconds") {
    get("/control/stop")
    actorSystem.awaitTermination(2.seconds)
    actorSystem should be ('terminated)
  }

  override def afterAll() {
    actorSystem.shutdown()
  }
}