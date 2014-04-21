package magnify.model.graph

import scala.collection.mutable
import scala.collection.JavaConversions._

import com.tinkerpop.blueprints.{Graph => BlueprintsGraph, _}
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.pipes.PipeFunction
import magnify.model.ChangeDescription

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

  private var headVertex: Vertex = _
  private var parentRevVertex: Option[Vertex] = None
  private val currentVerticesByKey: mutable.Map[VertexKey, Vertex] = mutable.Map[VertexKey, Vertex]()

  def revVertices(rev: Option[String] = None): GremlinPipeline[Vertex, Vertex] = {
    val revVertex = rev.flatMap { (revId) =>
      val commitVertices = vertices.has("kind", "commit").has("name", revId).toList
      if (commitVertices.size() == 1) { Some(commitVertices.get(0).asInstanceOf[Vertex]) } else { None }
    }.getOrElse(headVertex)
    new GremlinPipeline().start(revVertex).in("in-revision")
  }

  lazy val currentVerticesFilter: PipeFunction[Vertex, java.lang.Boolean] =
    new PipeFunction[Vertex, java.lang.Boolean]() {
      override def compute(argument: Vertex): java.lang.Boolean = currentVerticesByKey.values.contains(argument)
    }

  def currentVertices: GremlinPipeline[Vertex, Vertex] = vertices.filter(currentVerticesFilter)

  def vertices: GremlinPipeline[Vertex, Vertex] =
    new GremlinPipeline(blueprintsGraph.getVertices, true)

  // edges between elements in a given revision
  // TODO: Implement filtering
  def edges(rev: Option[String] = None): GremlinPipeline[Edge, Edge] =
    new GremlinPipeline(blueprintsGraph.getEdges, true)

  def addVertex(kind: String, name: String): (Vertex, Option[Vertex]) = {
    val vertexKey = VertexKey(kind, name)
    val oldVertex = currentVerticesByKey.get(vertexKey)
    val newVertex = rawAddVertex(kind, name)
    currentVerticesByKey.put(vertexKey, newVertex)
    (newVertex, oldVertex)
  }

  private def rawAddVertex(kind: String, name: String) = {
    val newVertex = blueprintsGraph.addVertex(null)
    newVertex.setProperty("kind", kind)
    newVertex.setProperty("name", name)
    newVertex
  }

  def removeFromCurrent(kind: String, name: String): Unit = currentVerticesByKey -= VertexKey(kind, name)

  def addEdge(from: Vertex, label: String, to: Vertex): Edge =
    blueprintsGraph.addEdge(null, from, to, label)

  def commitVersion(changeDescription: ChangeDescription): Unit = {
    val revVertex = rawAddVertex("commit", changeDescription.revision)
    changeDescription.setProperties(revVertex)
    headVertex = parentRevVertex.map { parentRev =>
      this.addEdge(revVertex, "commit", parentRev)
      headVertex
    }.getOrElse(revVertex)
    for (inRevVertex <- currentVerticesByKey.values) {
      this.addEdge(inRevVertex, "in-revision", revVertex)
    }
    parentRevVertex = Some(revVertex)
  }

  private case class VertexKey(kind: String, name: String)
}
