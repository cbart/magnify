package magnify.services

import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Repository {
  def addProject(name: String, graph: Graph): Repository

  def projects: Seq[String]

  def get(name: String): Option[Graph]
}
