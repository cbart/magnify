package magnify.features

import shapeless.~>
import java.io.InputStream
import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] object ExtractGraph {
  type Reader = ({type λ[A] = InputStream => Seq[A]})#λ ~> Seq
}

private[features] final class ExtractGraph[AST]
    (parser: InputStream => Seq[AST], factory: Seq[AST] => Graph)
    extends (ExtractGraph.Reader => Graph) {
  override def apply(reader: ExtractGraph.Reader): Graph = factory(reader(parser))
}
