package magnify.services

import magnify.model.java.Ast
import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class ResolveImports extends (Seq[Ast] => Graph) {
  def apply(classes: Seq[Ast]): Graph = {
    val classNames = classes.map(_.className).toSet
    val imports = for {
      Ast(imports, name) <- classes
    } yield (name, imports.filter(classNames))
    Graph(imports.toMap)
  }
}
