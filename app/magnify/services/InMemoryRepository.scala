package magnify.services

import magnify.model.graph.Graph
import magnify.features.GraphRepository

final case class InMemoryRepository (private var memory: Map[String, Graph])
    extends GraphRepository {
  override def add(name: String, graph: Graph) {
    memory = memory.updated(name, graph)
  }

  override def names: Seq[String] =
    memory.keys.toSeq

  override def get(name: String): Option[Graph] =
    memory.get(name)

  override def contains(name: String): Boolean =
    memory.contains(name)
}
