package magnify.features

import com.tinkerpop.blueprints.Graph
import magnify.model.Archive

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Sources {
  def add(name: String, file: Archive)

  def list: Seq[String]

  def get(name: String): Option[Graph]
}
