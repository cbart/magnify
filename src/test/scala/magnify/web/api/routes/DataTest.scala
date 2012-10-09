package magnify.web.api.routes

import magnify.core
import magnify.testing.web.{StoppingActorSystem, WebApiTest}
import magnify.web.api

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestKit
import akka.actor.ActorSystem

@RunWith(classOf[JUnitRunner])
class DataTest extends TestKit(ActorSystem()) with FunSuite with ShouldMatchers
    with BeforeAndAfterAll with WebApiTest with StoppingActorSystem with core.Module
    with api.Module {
  test("GET /data/projects/list.json should respond with stubbed project list") {
    get("/data/projects/list.json").asString should equal("LIST.JSON")
  }

  test("GET /data/projects/$PROJECT/head/whole.gexf should respond with stubbed GEXF") {
    get("/data/projects/wicked/head/whole.gexf").asString should equal("whole.gexf for wicked")
  }

  test("PUT /data/projects/$PROJECT/head/calls should respond with stubbed message") {
    put("/data/projects/wizard/head/calls").asString should equal("uploaded calls.csv for wizard")
  }

  test("POST /data/projects should respond with stubbed message") {
    post("/data/projects").asString should equal("created new project from zip sources")
  }
}
