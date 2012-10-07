package magnify.web.api.routes

import akka.actor.ActorSystem
import cc.spray.Directives
import cc.spray.directives.{PathEnd, PathElement, IntNumber}

/**
 * Data manipulation HTTP REST api.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Data (override implicit val actorSystem: ActorSystem) extends Directives {
  val route = {
    pathPrefix("data" / "projects") {
      (path("list.json") & get) {
        completeWith("LIST.JSON")
      } ~
      pathPrefix(PathElement) { project =>
        pathPrefix("head") {
          (path("whole.gexf") & get) {
            completeWith("whole.gexf for %s".format(project))
          } ~
          (path("calls") & put) {
            completeWith("uploaded calls.csv for %s".format(project))
          }
        }
      } ~
      (path(PathEnd) & post) {
        completeWith("created new project from zip sources")
      }
    }
  }
}
