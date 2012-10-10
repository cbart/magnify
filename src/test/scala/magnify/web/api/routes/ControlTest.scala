package magnify.web.api.routes

import magnify.core.Core
import magnify.testing.web.{StoppingActorSystem, WebApiTest}

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.util.duration._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class ControlTest extends TestKit(ActorSystem()) with FunSuite with ShouldMatchers with WebApiTest
    with StoppingActorSystem with Core with Routes {
  test("GET /control/stop should kill the actor system in less than 2 seconds") {
    get("/control/stop")
    actorSystem.awaitTermination(2.seconds)
    actorSystem should be ('terminated)
  }
}