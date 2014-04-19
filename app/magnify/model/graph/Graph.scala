package magnify.model.graph

import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.{Graph => BlueprintsGraph, _}
import com.tinkerpop.gremlin.java.GremlinPipeline
import scala.collection.JavaConversions._
import scala.collection.mutable
import magnify.model.ChangeDescription
import com.tinkerpop.pipes.PipeFunction

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
object Graph {
  implicit def gremlinPipelineAsScalaIterable[S, E](pipe: GremlinPipeline[S, E]): Iterable[E] =
    collectionAsScalaIterable(pipe.toList)

  def tinker: Graph =
    new Graph(new TinkerGraph)
}

final class Graph (val blueprintsGraph: BlueprintsGraph) {

  private var atHead = true
  private val headCommitVertices: mutable.Set[Vertex] = mutable.Set[Vertex]()
  private val currentVerticesByKey: mutable.Map[VertexKey, Vertex] = mutable.Map[VertexKey, Vertex]()

  def headVertices: GremlinPipeline[Vertex, Vertex] = vertices.filter(
    new PipeFunction[Vertex, java.lang.Boolean]() {
      override def compute(argument: Vertex): java.lang.Boolean = headCommitVertices.contains(argument)
    })


  def currentVertices: GremlinPipeline[Vertex, Vertex] = vertices.filter(
    new PipeFunction[Vertex, java.lang.Boolean]() {
      override def compute(argument: Vertex): java.lang.Boolean = currentVerticesByKey.values.toSet.contains(argument)
    })

  def vertices: GremlinPipeline[Vertex, Vertex] =
    new GremlinPipeline(blueprintsGraph.getVertices, true)

  def edges: GremlinPipeline[Edge, Edge] =
    new GremlinPipeline(blueprintsGraph.getEdges, true)

  def addVertex(kind: String, name: String): (Vertex, Option[Vertex]) = {
    val vertexKey = VertexKey(kind, name)
    val oldVertex = currentVerticesByKey.get(vertexKey)
    val newVertex = blueprintsGraph.addVertex(null)
    newVertex.setProperty("kind", kind)
    newVertex.setProperty("name", name)
    if (atHead) { headCommitVertices.add(newVertex) }
    currentVerticesByKey.put(vertexKey, newVertex)
    (newVertex, oldVertex)
  }

  def addEdge(from: Vertex, label: String, to: Vertex): Edge =
    blueprintsGraph.addEdge(null, from, to, label)

  def commitVersion(changeDescription: Option[ChangeDescription]) = atHead = false

  private case class VertexKey(kind: String, name: String)
}
