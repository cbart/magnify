package magnify.model

import magnify.model.graph.Graph
import com.tinkerpop.blueprints.{Edge, Vertex}

final case class ChangeDescription(
    revision: String,
    description: String,
    author: String,
    committer: String,
    time: Int,
    changedFiles: Map[Option[String], Option[String]]) {
  def revisionEdge(graph: Graph, current: Vertex, newer: Vertex): Edge = {
    val edge = graph.addEdge(current, "commit", newer)
    edge.setProperty("rev", revision)
    edge.setProperty("desc", description)
    edge.setProperty("author", author)
    edge.setProperty("committer", committer)
    edge.setProperty("time", time)
    edge
  }
}
