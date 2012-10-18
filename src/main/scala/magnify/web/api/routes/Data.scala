package magnify.web.api.routes

import akka.actor.ActorSystem
import cc.spray.{Directives, Route}
import cc.spray.directives.{PathEnd, PathElement}

/**
 * Data manipulation REST routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Data (system: ActorSystem) extends (() => Route) {
  val directives = Directives(system)

  import directives._

  override def apply: Route = {
    pathPrefix("data" / "projects") {
      (path("list.json") & get) {
        completeWith("LIST.JSON")
      } ~
      pathPrefix(PathElement) { project =>
        pathPrefix("head") {
          (path("whole.gexf") & get) {
            completeWith("whole.gexf for project %s".format(project))
          } ~
          (path("calls") & post) {
            completeWith("uploaded calls.csv for %s".format(project))
          }
        }
      } ~
      (path(PathEnd) & post) {
        completeWith("zip uploaded")
      }
    }
  }
}