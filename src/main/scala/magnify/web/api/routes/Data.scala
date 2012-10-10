package magnify.web.api.routes

import magnify.core.Core

import cc.spray.Directives
import cc.spray.directives.{PathEnd, PathElement}

/**
 * Data manipulation HTTP REST api.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Data {
  this: Core =>

  private[routes] final class DataDirectives extends Directives {
    override def actorSystem = Data.this.actorSystem

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

  private[routes] val dataDirectives = new DataDirectives
}
