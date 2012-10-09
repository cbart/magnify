package magnify.web.api.routes

import magnify.core
import magnify.web.api
import magnify.testing.web.{StoppingActorSystem, WebApiTest}

import akka.util.duration._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import akka.actor.ActorSystem
import akka.testkit.TestKit

@RunWith(classOf[JUnitRunner])
class ControlTest extends TestKit(ActorSystem()) with FunSuite with ShouldMatchers with WebApiTest
    with StoppingActorSystem with core.Module with api.Module {
  test("GET /control/stop should kill the actor system in less than 2 seconds") {
    get("/control/stop")
    actorSystem.awaitTermination(2.seconds)
    actorSystem should be ('terminated)
  }
}