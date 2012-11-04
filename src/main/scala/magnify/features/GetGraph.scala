package magnify.features

import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class GetGraph (repository: GraphRepository) extends GraphUseCase {
  override def get(name: String): Option[Graph] = repository.get(name)
}
