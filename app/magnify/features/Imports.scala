package magnify.features

import magnify.model.java.Ast

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Imports {
  /**
   * Resolves imports from given `Ast` `Seq` yielding a who imports who `Map` containing classes
   * FQNs.
   */
  def resolve(asts: Seq[Ast]): Map[String, Seq[String]]
}