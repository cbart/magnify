package magnify.features.project.graph

import akka.actor._
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.google.inject.{Provider, Inject}
import com.google.inject.name.Named
import magnify.services.project.repository.GetGraph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final private[features] class ProjectGraph @Inject() (actorSystem: ActorSystem,
    @Named("project-repository") repository: ActorRef) extends Provider[ActorRef] {
  override def get: ActorRef = actorSystem.actorOf(
    props = Props(
      new Actor {
        override protected def receive = {
          case GetProjectGraph(projectName, continuation) =>
            repository ! GetGraph(projectName, g => continuation(g.getOrElse(exampleGraph)))
        }
      }
    ),
    name = "project-graph"
  )

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
