package magnify.web.api.routes

import magnify.web.api.controllers.DataController

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar._

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class DataTest extends RouteTestBase {
  val controller = mock[DataController]

  val route = new Data(system).apply

  test("should render projects list") {
    Get("/data/projects/list.json") ~> route ~> check {
      entityAs[String] should be("LIST.JSON")
    }
  }

  test("should render whole.gexf") {
    Get("/data/projects/0/head/whole.gexf") ~> route ~> check {
      entityAs[String] should be("whole.gexf for project 0")
    }
  }

  test("should respond to calls post") {
    Post("/data/projects/0/head/calls") ~> route ~> check {
      entityAs[String] should be("uploaded calls.csv for 0")
    }
  }

  test("should respond to zip post") {
    Post("/data/projects") ~> route ~> check {
      entityAs[String] should be("temp")
    }
  }
}
