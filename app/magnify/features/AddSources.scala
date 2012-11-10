package magnify.features

import magnify.model.graph.Graph
import magnify.services.{JavaParser, Reader}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class AddSources (parser: JavaParser, repository: GraphRepository,
    imports: Imports) extends SourceUseCase {
  override def add(name: String, sources: Reader) {
    val asts = sources.read(parser)
    val importMapping = imports.resolve(asts)
    repository.add(name, Graph(importMapping))
  }
}
