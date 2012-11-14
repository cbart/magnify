package magnify.model.graph

import com.tinkerpop.blueprints.{Graph => BlueprintsGraph, Edge, Vertex}
import com.tinkerpop.gremlin.java.GremlinPipeline
import scala.collection.JavaConversions._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import java.util.Collections

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
object Graph {
  implicit def gremlinPipelineAsScalaIterable[S, E](pipe: GremlinPipeline[S, E]): Iterable[E] =
    collectionAsScalaIterable(pipe.toList)

  def tinker: Graph =
    new Graph(new TinkerGraph())
}

final class Graph (blueprintsGraph: BlueprintsGraph) {
  def vertices: GremlinPipeline[java.lang.Iterable[Vertex], java.lang.Iterable[Vertex]] = {
    val pipeline = new GremlinPipeline[java.lang.Iterable[Vertex], java.lang.Iterable[Vertex]]()
    pipeline.start(blueprintsGraph.getVertices)
    pipeline
  }

  def edges: GremlinPipeline[java.lang.Iterable[Edge], java.lang.Iterable[Edge]] = {
    val pipeline = new GremlinPipeline[java.lang.Iterable[Edge], java.lang.Iterable[Edge]]()
    pipeline.start(blueprintsGraph.getEdges)
    pipeline
  }

  def addVertex: Vertex =
    blueprintsGraph.addVertex(null)

  def addEdge(from: Vertex, label: String, to: Vertex): Edge =
    blueprintsGraph.addEdge(null, from, to, label)
}
