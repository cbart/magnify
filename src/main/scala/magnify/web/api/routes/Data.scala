package magnify.web.api.routes

import akka.actor.ActorSystem
import cc.spray.{Directives, Route}
import cc.spray.directives.{PathEnd, PathElement}
import cc.spray.http.HttpMethods.{GET, PUT, POST}
import com.google.inject.{Provider, Inject}

/**
 * Data manipulation REST routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Data @Inject() (actorSystem: ActorSystem) extends Provider[Route] {
  val directives = Directives(actorSystem)

  import directives._

  override def get: Route = {
    pathPrefix("data" / "projects") {
      (path("list.json") & method(GET)) { context =>
        completeWith("LIST.JSON")
      } ~
      pathPrefix(PathElement) { project =>
        pathPrefix("head") {
          (path("whole.gexf") & method(GET)) {
            completeWith("whole.gexf for %s".format(project))
          } ~
          (path("calls") & method(PUT)) {
            completeWith("uploaded calls.csv for %s".format(project))
          }
        }
      } ~
      (path(PathEnd) & method(POST)) {
        completeWith("created new project from zip sources")
      }
    }
  }

}
