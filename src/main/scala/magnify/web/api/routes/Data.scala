package magnify.web.api.routes

import akka.actor.{ActorRef, ActorSystem}
import cc.spray.{RequestContext, Directives, Route}
import cc.spray.directives.{PathEnd, PathElement}
import cc.spray.http.HttpMethods.{GET, PUT, POST}
import com.google.inject.{Provider, Inject}
import com.google.inject.name.Named
import magnify.core.GetProjectGraph
import com.tinkerpop.blueprints.Graph
import magnify.web.api.view.gexf.GexfViewModel

/**
 * Data manipulation REST routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Data @Inject() (actorSystem: ActorSystem,
    @Named("project-graph") projectGraph: ActorRef,
    @Named("view") gexfView: ActorRef) extends Provider[Route] {
  val directives = Directives(actorSystem)

  import directives._

  override def get: Route = {
    pathPrefix("data" / "projects") {
      (path("list.json") & method(GET)) { context =>
        completeWith("LIST.JSON")
      } ~
      pathPrefix(PathElement) { project =>
        pathPrefix("head") {
          (path("whole.gexf") & method(GET)) { context =>
            projectGraph ! GetProjectGraph(project, renderGexfTo(context) _)
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

  def renderGexfTo(context: RequestContext)(graph: Graph) {
    gexfView ! GexfViewModel(graph, context complete _)
  }
}