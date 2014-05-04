package magnify.model.graph

import java.lang

import scala.collection.JavaConversions._

import com.tinkerpop.blueprints.{Graph => BlueprintsGraph, _}
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.pipes.PipeFunction
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
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

  private def revVertex(rev: Option[String]): Vertex = {
    rev.flatMap { (revId) =>
      val commitVertices = vertices.has("kind", "commit").has("name", revId).toList
      if (commitVertices.size() == 1) { Some(commitVertices.get(0).asInstanceOf[Vertex]) } else { None }
    }.getOrElse(headVertex)
  }

  def revVertices(rev: Option[String] = None): GremlinPipeline[Vertex, Vertex] = {
    new GremlinPipeline().start(revVertex(rev)).in("in-revision")
  }

  def commitsOlderThan(rev: Option[String] = None): Seq[Vertex] = {
    val head = revVertex(rev)
    val older = new GremlinPipeline().start(head).in("commit").loop(1, TrueFilter, TrueFilter).dedup().toList.toSeq
    Seq(head) ++ older
  }

  def currentVertices: GremlinPipeline[Vertex, Vertex] =
    parentRevVertex.map { revVertex =>
      new GremlinPipeline().start(revVertex).in("in-revision")
      .transform(NewVertex)
    }.getOrElse(vertices)

  def vertices: GremlinPipeline[Vertex, Vertex] =
    new GremlinPipeline(blueprintsGraph.getVertices, true)

  // edges between elements in a given revision
  // TODO: Implement filtering
  def edges(rev: Option[String] = None): GremlinPipeline[Edge, Edge] =
    new GremlinPipeline(blueprintsGraph.getEdges, true)

  def getPrevCommitVertex(kind: String, name: String, props: Map[String, String]): Option[Vertex] = {
    var pipeline = currentVertices.has("kind", kind).has("name", name)
    props.keys.foreach((prop) => pipeline = pipeline.has(prop, props(prop)))
    val vertex = pipeline.transform(new AsVertex).dedup().toList.toSet
    require(vertex.size <= 1, parentRevVertex.map(_.getProperty("rev")) + " : " + vertex.map((v) => "V[" + Seq(v
        .getProperty("name"), v.getProperty("kind"), v.getId.toString).mkString(", ") + "]").mkString(", "))
    vertex.headOption
  }

  def addVertex(kind: String, name: String, props: Map[String, String] = Map()): (Vertex, Option[Edge]) = {
    val oldVertex: Option[Vertex] = getPrevCommitVertex(kind, name, props)
    val newVertex = rawAddVertex(kind, name)
    props.keys.foreach((prop) => newVertex.setProperty(prop, props(prop)))
    (newVertex, oldVertex.map(addEdge(newVertex, "commit", _)))
  }

  private def rawAddVertex(kind: String, name: String) = {
    val newVertex = blueprintsGraph.addVertex(null)
    newVertex.setProperty("kind", kind)
    newVertex.setProperty("name", name)
    newVertex
  }

  def addEdge(from: Vertex, label: String, to: Vertex): Edge =
    blueprintsGraph.addEdge(null, from, to, label)

  def commitVersion(changeDescription: ChangeDescription, classes: Set[String]): Unit = {
    val revVertex = rawAddVertex("commit", changeDescription.revision)
    changeDescription.setProperties(revVertex)
    headVertex = parentRevVertex.map { parentRev =>
      this.addEdge(revVertex, "commit", parentRev)
      headVertex
    }.getOrElse(revVertex)

    val currentClasses = currentVertices
        .has("kind", "class")
        .filter(HasInFilter("name", classes))
        .filter(NotFilter(HasInFilter("file-name", changeDescription.removedFiles)))
        .transform(new AsVertex())
    val classVertices = currentClasses.toList
    val currentPackages =
      new GremlinPipeline(classVertices, true).out("in-package").loop(1, TrueFilter, TrueFilter).dedup()
    val pkgVertices = currentPackages.toList
    for (inRevVertex <- classVertices ++ pkgVertices) {
      this.addEdge(inRevVertex, "in-revision", revVertex)
    }
    parentRevVertex = Some(revVertex)
  }

  private case class HasInFilter[T <: Element](property: String, values: Set[String])
      extends PipeFunction[T, lang.Boolean] {
    override def compute(element: T): lang.Boolean = values.contains(element.getProperty(property))
  }

  private case class NotFilter[T <: Element](filter: PipeFunction[T, lang.Boolean])
      extends PipeFunction[T, lang.Boolean] {
    override def compute(argument: T): lang.Boolean = !filter.compute(argument)
  }

  private object TrueFilter extends PipeFunction[LoopBundle[Vertex], lang.Boolean] {
    override def compute(argument: LoopBundle[Vertex]): lang.Boolean = true
  }

  private object NewVertex extends PipeFunction[Vertex, Vertex] {
    override def compute(v: Vertex): Vertex = {
      val it = v.getVertices(Direction.OUT, "commit").iterator()
      if (it.hasNext) { it.next() } else { v }
    }
  }

  private class AsVertex[T <: Element] extends PipeFunction[T, Vertex] {
    override def compute(argument: T): Vertex = argument.asInstanceOf[Vertex]
  }
}
