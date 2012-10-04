package magnify.server

import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.impls.tg.TinkerGraph

import akka.util.Duration
import akka.util.duration.intToDurationInt
import cc.spray.Directives
import cc.spray.http.MediaTypes._

trait GraphService extends Directives {
  import Gexf._

  val `application/gexf+xml` = register(CustomMediaType("application/gexf+xml"))

  val graphService = {
    pathPrefix("www") {
      respondWithMediaType(`text/html`) {
        getFromResourceDirectory("www")
      }
    } ~
    pathPrefix("js") {
      respondWithMediaType(`application/javascript`) {
    	getFromResourceDirectory("javascript")
      }
    } ~
    path("graph.gexf") {
      respondWithMediaType(`application/gexf+xml`) {
    	completeWith(exampleGraph.toXml)
      }
    } ~
    path("stop") { ctx =>
      ctx.complete("Shutting down in 1 second...")
      in(1000.millis) {
        actorSystem.shutdown()
      }
    }
  }

  def in[U](duration: Duration)(body: => U) {
    actorSystem.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
  }

  private val numberOfNodes = 100
  private val numberOfEdges = 300

  private def exampleGraph: Graph = {
    val random = new scala.util.Random()
    val graph = new TinkerGraph()
    val nodes = 0 until numberOfNodes map { _ => graph.addVertex(null) }
    (0 until numberOfEdges) foreach { _ =>
      val source = nodes(random.nextInt.abs % numberOfNodes)
      val destination = nodes(random.nextInt.abs % numberOfNodes)
      graph.addEdge(null, source, destination, random.nextString(2))
    }
    graph
  }
}