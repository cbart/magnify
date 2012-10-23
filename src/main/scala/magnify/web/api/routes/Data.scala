package magnify.web.api.routes

import magnify.web.api.controllers.DataController

import akka.actor.ActorSystem
import spray.routing._

/**
 * Data manipulation REST routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Data (implicit system: ActorSystem, controller: DataController)
    extends (() => Route) {
  import Directives._

  override def apply: Route = {
    pathPrefix("data" / "projects") {
      (path("list.json") & get) {
        complete("LIST.JSON")
      } ~
      pathPrefix(PathElement) { project =>
        pathPrefix("head") {
          (path("whole.gexf") & get) {
            complete("whole.gexf for project %s".format(project))
          } ~
          (path("calls") & post) {
            complete("uploaded calls.csv for %s".format(project))
          }
        }
      } ~
      (path(PathEnd) & post) {
        controller uploadSources _
      }
    }
  }
}