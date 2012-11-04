package magnify.features

import magnify.model.graph.Graph
import magnify.model.java.Ast

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class AddSources (repository: GraphRepository, imports: Imports)
    extends SourceUseCase {
  override def add(name: String, sources: Seq[Ast]) {
    val importMapping = imports.resolve(sources)
    repository.add(name, Graph(importMapping))
  }
}
