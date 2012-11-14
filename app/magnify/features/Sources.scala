package magnify.features

import magnify.model.Archive
import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Sources {
  def add(name: String, file: Archive)

  def list: Seq[String]

  def get(name: String): Option[Graph]
}
