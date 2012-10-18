package magnify.services.project

import com.tinkerpop.blueprints.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Repository {
  def addProject(name: String, graph: Graph)

  def listProjects(ĸ: Seq[String] => Unit)

  def getProject(name: String, ĸ: Graph => Unit)
}
