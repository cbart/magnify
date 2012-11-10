package magnify.features

import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait GraphRepository {
  def add(name: String, graph: Graph)

  def names: Seq[String]

  def get(name: String): Option[Graph]

  def contains(name: String): Boolean
}
