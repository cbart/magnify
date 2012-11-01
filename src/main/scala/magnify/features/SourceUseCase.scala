package magnify.features

import magnify.model.java.Ast

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait SourceUseCase {
  def add(name: String, sources: Seq[Ast])
}
