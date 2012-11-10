package magnify.features

import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait GraphUseCase {
  def get(name: String): Option[Graph]
}
